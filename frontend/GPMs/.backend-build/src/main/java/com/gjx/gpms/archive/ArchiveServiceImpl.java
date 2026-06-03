package com.gjx.gpms.archive;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.entity.*;
import com.gjx.gpms.mapper.*;
import com.gjx.gpms.mq.producer.OperationLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 归档实现：
 * 将非当前批次的数据从 current 表迁移到 history 表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    /**
     * 批次表操作
     */
    private final BatchMapper batchMapper;

    /**
     * 归档日志表操作
     */
    private final ArchiveLogMapper archiveLogMapper;

    /**
     * 当前题目表
     */
    private final TopicCurrentMapper topicCurrentMapper;

    /**
     * 历史题目表
     */
    private final TopicHistoryMapper topicHistoryMapper;

    /**
     * 当前选题表
     */
    private final SelectionCurrentMapper selectionCurrentMapper;

    /**
     * 历史选题表
     */
    private final SelectionHistoryMapper selectionHistoryMapper;

    /**
     * 当前成绩表
     */
    private final ScoreCurrentMapper scoreCurrentMapper;

    /**
     * 历史成绩表
     */
    private final ScoreHistoryMapper scoreHistoryMapper;

    /**
     * 当前周报表
     */
    private final WeeklyLogCurrentMapper weeklyLogCurrentMapper;

    /**
     * 历史周报表
     */
    private final WeeklyLogHistoryMapper weeklyLogHistoryMapper;

    /**
     * 学生选题表
     */
    private final StudentTopicMapper studentTopicMapper;

    /**
     * MQ消息发送器，用于记录归档操作日志
     */
    private final OperationLogProducer operationLogProducer;

    /**
     * 手动归档指定批次
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveBatch(Long batchId, Long operatorId) {

        // 查询批次信息
        Batch batch = batchMapper.selectById(batchId);

        // 批次不存在直接报错
        if (batch == null) {
            throw new BusinessException("归档批次不存在");
        }

        // 当前进行中的批次禁止归档
        if (batch.getStatus() != null && batch.getStatus() == 1) {
            throw new BusinessException("当前进行中的批次不能归档");
        }

        // 查询是否已经归档成功过
        Long successCount = archiveLogMapper.selectCount(
                new LambdaQueryWrapper<ArchiveLog>()
                        .eq(ArchiveLog::getBatchId, batchId)
                        .eq(ArchiveLog::getStatus, "SUCCESS")
        );

        // 防止重复归档
        if (successCount > 0) {
            throw new BusinessException("该批次已归档，请勿重复归档");
        }

        // 记录归档开始日志
        log.info("开始归档批次数据，batchId={}, operatorId={}", batchId, operatorId);

        // 归档题目数据
        int topicCount = archiveTopic(batchId, operatorId);

        // 归档选题数据
        int selectionCount = archiveSelection(batchId, operatorId);

        // 归档成绩数据
        int scoreCount = archiveScore(batchId, operatorId);

        // 归档周报数据
        int weeklyLogCount = archiveWeeklyLog(batchId, operatorId);

        // 保存归档记录
        ArchiveLog archiveLog = new ArchiveLog();
        archiveLog.setBatchId(batchId);
        archiveLog.setOperatorId(operatorId);
        archiveLog.setTopicCount(topicCount);
        archiveLog.setSelectionCount(selectionCount);
        archiveLog.setScoreCount(scoreCount);
        archiveLog.setWeeklyLogCount(weeklyLogCount);
        archiveLog.setStatus("SUCCESS");
        archiveLog.setMessage("归档成功");

        archiveLogMapper.insert(archiveLog);

        // MQ发送归档日志
        operationLogProducer.send(
                operatorId,
                "ARCHIVE",
                "batch",
                String.valueOf(batchId),
                "批次冷热数据归档成功"
        );

        // 记录归档结束日志
        log.info("批次归档完成，batchId={}, topic={}, selection={}, score={}, weeklyLog={}",
                batchId, topicCount, selectionCount, scoreCount, weeklyLogCount);
    }

    /**
     * 归档所有历史批次
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveHistoryBatches(Long operatorId) {

        // 查询所有已结束批次(status=0)
        List<Long> batchIds = batchMapper.selectList(
                        new LambdaQueryWrapper<Batch>()
                                .eq(Batch::getStatus, 0)
                ).stream()
                .map(Batch::getId)
                .collect(Collectors.toList());

        // 遍历归档
        for (Long batchId : batchIds) {

            // 判断该批次是否归档过
            Long successCount = archiveLogMapper.selectCount(
                    new LambdaQueryWrapper<ArchiveLog>()
                            .eq(ArchiveLog::getBatchId, batchId)
                            .eq(ArchiveLog::getStatus, "SUCCESS")
            );

            // 没归档过才执行归档
            if (successCount == 0) {
                archiveBatch(batchId, operatorId);
            }
        }
    }

    /**
     * 定时归档任务
     * 每天凌晨2:30执行
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void scheduledArchiveHistoryBatches() {
        try {

            // 自动归档历史批次
            archiveHistoryBatches(null);

        } catch (Exception e) {

            // 定时任务异常记录日志
            log.error("定时归档历史批次失败", e);
        }
    }

    /**
     * 归档题目数据
     */
    private int archiveTopic(Long batchId, Long operatorId) {

        // 查询当前批次所有题目
        List<TopicCurrent> sourceList = topicCurrentMapper.selectList(
                new LambdaQueryWrapper<TopicCurrent>()
                        .eq(TopicCurrent::getBatchId, batchId)
        );

        int sourceCount = sourceList.size();
        int inserted = 0;

        // 当前归档时间
        LocalDateTime archiveTime = LocalDateTime.now();

        for (TopicCurrent source : sourceList) {

            // current对象转history对象
            TopicHistory history =
                    toTopicHistory(source, batchId, operatorId, archiveTime);

            // 避免重复插入
            if (topicHistoryMapper.selectById(history.getId()) == null) {
                inserted += topicHistoryMapper.insert(history);
            }
        }

        // 校验归档数量
        verifyArchiveCount("topic", sourceCount, inserted);

        // 删除当前表数据
        topicCurrentMapper.delete(
                new LambdaQueryWrapper<TopicCurrent>()
                        .eq(TopicCurrent::getBatchId, batchId)
        );

        return inserted;
    }

    /**
     * 归档选题数据
     */
    private int archiveSelection(Long batchId, Long operatorId) {

        List<SelectionCurrent> sourceList = selectionCurrentMapper.selectList(
                new LambdaQueryWrapper<SelectionCurrent>()
                        .eq(SelectionCurrent::getBatchId, batchId)
        );

        int sourceCount = sourceList.size();
        int inserted = 0;

        LocalDateTime archiveTime = LocalDateTime.now();

        for (SelectionCurrent source : sourceList) {

            SelectionHistory history =
                    toSelectionHistory(source, batchId, operatorId, archiveTime);

            if (selectionHistoryMapper.selectById(history.getId()) == null) {
                inserted += selectionHistoryMapper.insert(history);
            }
        }

        verifyArchiveCount("selection", sourceCount, inserted);

        selectionCurrentMapper.delete(
                new LambdaQueryWrapper<SelectionCurrent>()
                        .eq(SelectionCurrent::getBatchId, batchId)
        );

        return inserted;
    }

    /**
     * 归档成绩数据
     */
    private int archiveScore(Long batchId, Long operatorId) {

        List<ScoreCurrent> sourceList = scoreCurrentMapper.selectList(
                new LambdaQueryWrapper<ScoreCurrent>()
                        .eq(ScoreCurrent::getBatchId, batchId)
        );

        int sourceCount = sourceList.size();
        int inserted = 0;

        LocalDateTime archiveTime = LocalDateTime.now();

        for (ScoreCurrent source : sourceList) {

            ScoreHistory history =
                    toScoreHistory(source, batchId, operatorId, archiveTime);

            if (scoreHistoryMapper.selectById(history.getId()) == null) {
                inserted += scoreHistoryMapper.insert(history);
            }
        }

        verifyArchiveCount("score", sourceCount, inserted);

        scoreCurrentMapper.delete(
                new LambdaQueryWrapper<ScoreCurrent>()
                        .eq(ScoreCurrent::getBatchId, batchId)
        );

        return inserted;
    }

    /**
     * 归档周报数据
     */
    private int archiveWeeklyLog(Long batchId, Long operatorId) {

        // 查询该批次下所有 studentTopicId
        List<Long> studentTopicIds = studentTopicMapper.selectList(
                        new LambdaQueryWrapper<StudentTopic>()
                                .eq(StudentTopic::getBatchId, batchId)
                ).stream()
                .map(StudentTopic::getId)
                .collect(Collectors.toList());

        // 没有学生选题直接返回
        if (studentTopicIds.isEmpty()) {
            return 0;
        }

        // 查询周报数据
        List<WeeklyLogCurrent> sourceList =
                weeklyLogCurrentMapper.selectList(
                        new LambdaQueryWrapper<WeeklyLogCurrent>()
                                .in(WeeklyLogCurrent::getStudentTopicId, studentTopicIds)
                );

        int sourceCount = sourceList.size();
        int inserted = 0;

        LocalDateTime archiveTime = LocalDateTime.now();

        for (WeeklyLogCurrent source : sourceList) {

            WeeklyLogHistory history =
                    toWeeklyLogHistory(source, batchId, operatorId, archiveTime);

            if (weeklyLogHistoryMapper.selectById(history.getId()) == null) {
                inserted += weeklyLogHistoryMapper.insert(history);
            }
        }

        verifyArchiveCount("weekly_log", sourceCount, inserted);

        // 删除当前周报
        weeklyLogCurrentMapper.delete(
                new LambdaQueryWrapper<WeeklyLogCurrent>()
                        .in(WeeklyLogCurrent::getStudentTopicId, studentTopicIds)
        );

        return inserted;
    }

    /**
     * TopicCurrent 转 TopicHistory
     */
    private TopicHistory toTopicHistory(TopicCurrent source, Long batchId,
                                        Long operatorId, LocalDateTime archiveTime) {
        TopicHistory history = new TopicHistory();

        // 属性复制
        BeanUtils.copyProperties(source, history);

        // 补充归档字段
        fillArchiveInfo(history, batchId, operatorId, archiveTime);

        return history;
    }

    /**
     * SelectionCurrent 转 SelectionHistory
     */
    private SelectionHistory toSelectionHistory(SelectionCurrent source,
                                                Long batchId,
                                                Long operatorId,
                                                LocalDateTime archiveTime) {
        SelectionHistory history = new SelectionHistory();
        BeanUtils.copyProperties(source, history);
        fillArchiveInfo(history, batchId, operatorId, archiveTime);
        return history;
    }

    /**
     * ScoreCurrent 转 ScoreHistory
     */
    private ScoreHistory toScoreHistory(ScoreCurrent source,
                                        Long batchId,
                                        Long operatorId,
                                        LocalDateTime archiveTime) {
        ScoreHistory history = new ScoreHistory();
        BeanUtils.copyProperties(source, history);
        fillArchiveInfo(history, batchId, operatorId, archiveTime);
        return history;
    }

    /**
     * WeeklyLogCurrent 转 WeeklyLogHistory
     */
    private WeeklyLogHistory toWeeklyLogHistory(WeeklyLogCurrent source,
                                                Long batchId,
                                                Long operatorId,
                                                LocalDateTime archiveTime) {
        WeeklyLogHistory history = new WeeklyLogHistory();
        BeanUtils.copyProperties(source, history);
        fillArchiveInfo(history, batchId, operatorId, archiveTime);
        return history;
    }

    /**
     * 填充题目归档信息
     */
    private void fillArchiveInfo(TopicHistory history,
                                 Long batchId,
                                 Long operatorId,
                                 LocalDateTime archiveTime) {
        history.setArchiveTime(archiveTime);
        history.setArchiveBatchId(batchId);
        history.setArchiveOperator(operatorId);
    }

    /**
     * 填充选题归档信息
     */
    private void fillArchiveInfo(SelectionHistory history,
                                 Long batchId,
                                 Long operatorId,
                                 LocalDateTime archiveTime) {
        history.setArchiveTime(archiveTime);
        history.setArchiveBatchId(batchId);
        history.setArchiveOperator(operatorId);
    }

    /**
     * 填充成绩归档信息
     */
    private void fillArchiveInfo(ScoreHistory history,
                                 Long batchId,
                                 Long operatorId,
                                 LocalDateTime archiveTime) {
        history.setArchiveTime(archiveTime);
        history.setArchiveBatchId(batchId);
        history.setArchiveOperator(operatorId);
    }

    /**
     * 填充周报归档信息
     */
    private void fillArchiveInfo(WeeklyLogHistory history,
                                 Long batchId,
                                 Long operatorId,
                                 LocalDateTime archiveTime) {
        history.setArchiveTime(archiveTime);
        history.setArchiveBatchId(batchId);
        history.setArchiveOperator(operatorId);
    }

    /**
     * 校验归档数量是否一致
     */
    private void verifyArchiveCount(String tableName,
                                    int sourceCount,
                                    int inserted) {

        // 如果插入数量小于源数据数量，说明归档失败
        if (inserted < sourceCount) {
            throw new BusinessException(
                    tableName + "归档数量校验失败，source="
                            + sourceCount
                            + ", inserted="
                            + inserted
            );
        }
    }
}