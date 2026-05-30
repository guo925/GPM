package com.gjx.gpms.archive;

import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mq.producer.OperationLogProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 归档实现：把非当前批次数据从 current 表迁移至 history 表。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private final JdbcTemplate jdbcTemplate;
    private final BatchMapper batchMapper;
    private final OperationLogProducer operationLogProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveBatch(Long batchId, Long operatorId) {
        Batch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("归档批次不存在");
        }
        if (batch.getStatus() != null && batch.getStatus() == 1) {
            throw new BusinessException("当前进行中的批次不能归档");
        }
        Integer successCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM archive_log WHERE batch_id=? AND status='SUCCESS'",
                Integer.class,
                batchId
        );
        if (successCount != null && successCount > 0) {
            throw new BusinessException("该批次已归档，请勿重复归档");
        }

        log.info("开始归档批次数据，batchId={}, operatorId={}", batchId, operatorId);
        int topicCount = archiveTopic(batchId, operatorId);
        int selectionCount = archiveSelection(batchId, operatorId);
        int scoreCount = archiveScore(batchId, operatorId);
        int weeklyLogCount = archiveWeeklyLog(batchId, operatorId);

        jdbcTemplate.update(
                "INSERT INTO archive_log(batch_id, operator_id, topic_count, selection_count, score_count, weekly_log_count, status, message) VALUES (?,?,?,?,?,?,?,?)",
                batchId,
                operatorId,
                topicCount,
                selectionCount,
                scoreCount,
                weeklyLogCount,
                "SUCCESS",
                "归档成功"
        );
        operationLogProducer.send(operatorId, "ARCHIVE", "batch", String.valueOf(batchId), "批次冷热数据归档成功");
        log.info("批次归档完成，batchId={}, topic={}, selection={}, score={}, weeklyLog={}",
                batchId, topicCount, selectionCount, scoreCount, weeklyLogCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveHistoryBatches(Long operatorId) {
        List<Long> batchIds = jdbcTemplate.queryForList("SELECT id FROM batch WHERE status=0", Long.class);
        for (Long batchId : batchIds) {
            Integer successCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM archive_log WHERE batch_id=? AND status='SUCCESS'",
                    Integer.class,
                    batchId
            );
            if (successCount == null || successCount == 0) {
                archiveBatch(batchId, operatorId);
            }
        }
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void scheduledArchiveHistoryBatches() {
        try {
            archiveHistoryBatches(null);
        } catch (Exception e) {
            log.error("定时归档历史批次失败", e);
        }
    }

    private int archiveTopic(Long batchId, Long operatorId) {
        int sourceCount = count("SELECT COUNT(*) FROM topic_current WHERE batch_id=?", batchId);
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO topic_history
                SELECT tc.*, NOW(), ?, ? FROM topic_current tc WHERE tc.batch_id=?
                """, batchId, operatorId, batchId);
        verifyArchiveCount("topic", sourceCount, inserted);
        jdbcTemplate.update("DELETE FROM topic_current WHERE batch_id=?", batchId);
        return inserted;
    }

    private int archiveSelection(Long batchId, Long operatorId) {
        int sourceCount = count("SELECT COUNT(*) FROM selection_current WHERE batch_id=?", batchId);
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO selection_history
                SELECT sc.*, NOW(), ?, ? FROM selection_current sc WHERE sc.batch_id=?
                """, batchId, operatorId, batchId);
        verifyArchiveCount("selection", sourceCount, inserted);
        jdbcTemplate.update("DELETE FROM selection_current WHERE batch_id=?", batchId);
        return inserted;
    }

    private int archiveScore(Long batchId, Long operatorId) {
        int sourceCount = count("SELECT COUNT(*) FROM score_current WHERE batch_id=?", batchId);
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO score_history
                SELECT sc.*, NOW(), ?, ? FROM score_current sc WHERE sc.batch_id=?
                """, batchId, operatorId, batchId);
        verifyArchiveCount("score", sourceCount, inserted);
        jdbcTemplate.update("DELETE FROM score_current WHERE batch_id=?", batchId);
        return inserted;
    }

    private int archiveWeeklyLog(Long batchId, Long operatorId) {
        int sourceCount = count("""
                SELECT COUNT(*) FROM weekly_log_current wlc
                INNER JOIN student_topic st ON st.id=wlc.student_topic_id
                WHERE st.batch_id=?
                """, batchId);
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO weekly_log_history
                SELECT wlc.*, NOW(), ?, ? FROM weekly_log_current wlc
                INNER JOIN student_topic st ON st.id=wlc.student_topic_id
                WHERE st.batch_id=?
                """, batchId, operatorId, batchId);
        verifyArchiveCount("weekly_log", sourceCount, inserted);
        jdbcTemplate.update("""
                DELETE wlc FROM weekly_log_current wlc
                INNER JOIN student_topic st ON st.id=wlc.student_topic_id
                WHERE st.batch_id=?
                """, batchId);
        return inserted;
    }

    private int count(String sql, Long batchId) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, batchId);
        return count == null ? 0 : count;
    }

    private void verifyArchiveCount(String tableName, int sourceCount, int inserted) {
        if (inserted < sourceCount) {
            throw new BusinessException(tableName + "归档数量校验失败，source=" + sourceCount + ", inserted=" + inserted);
        }
    }
}
