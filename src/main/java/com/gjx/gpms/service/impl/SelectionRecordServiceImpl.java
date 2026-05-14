package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.SelectionSubmitDTO;
import com.gjx.gpms.dto.TeacherReviewDTO;
import com.gjx.gpms.entity.*;
import com.gjx.gpms.mapper.*;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.SelectionRecordService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.SelectionRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 选题记录服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SelectionRecordServiceImpl extends ServiceImpl<SelectionRecordMapper, SelectionRecord> implements SelectionRecordService {

    private final BatchMapper batchMapper;
    private final TopicMapper topicMapper;
    private final StudentTopicMapper studentTopicMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void submitPreferences(SelectionSubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        log.info("学生[{}]提交选题志愿，批次[{}]", studentId, dto.getBatchId());

        Batch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        // 检查是否已经提交过
        Long existCount = this.count(
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(SelectionRecord::getBatchId, dto.getBatchId())
                        .eq(SelectionRecord::getStudentId, studentId)
        );

        if (existCount > 0) {
            throw new BusinessException("已提交过志愿，请勿重复提交");
        }

        // 检查志愿数是否超出限制
        if (dto.getTopicIds().size() > batch.getStudentMaxChoices()) {
            throw new BusinessException("最多选择" + batch.getStudentMaxChoices() + "个志愿");
        }

        // 保存志愿记录
        for (int i = 0; i < dto.getTopicIds().size(); i++) {
            Long topicId = dto.getTopicIds().get(i);

            Topic topic = topicMapper.selectById(topicId);
            if (topic == null || !"approved".equals(topic.getStatus())) {
                throw new BusinessException("课题[" + topicId + "]不存在或未审核通过");
            }

            SelectionRecord record = new SelectionRecord();
            record.setBatchId(dto.getBatchId());
            record.setStudentId(studentId);
            record.setTopicId(topicId);
            record.setPriority((byte) (i + 1));
            record.setIsSelected((byte) 0);
            this.save(record);
        }

        log.info("学生[{}]提交选题志愿成功，共{}个志愿", studentId, dto.getTopicIds().size());
    }

    @Override
    public List<SelectionRecordVO> getMySelections(Long batchId) {
        Long studentId = UserContext.getUserId();

        List<SelectionRecord> records = this.list(
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(batchId != null, SelectionRecord::getBatchId, batchId)
                        .eq(SelectionRecord::getStudentId, studentId)
                        .orderByAsc(SelectionRecord::getPriority)
        );

        return toVOList(records);
    }

    @Override
    public List<SelectionRecordVO> getTeacherReviewList(Long batchId) {
        Long teacherId = UserContext.getUserId();

        // 查找该教师创建的课题
        List<Topic> myTopics = topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .eq(batchId != null, Topic::getBatchId, batchId)
                        .eq(Topic::getCreatorId, teacherId)
        );

        List<Long> topicIds = myTopics.stream().map(Topic::getId).collect(Collectors.toList());

        if (topicIds.isEmpty()) {
            return List.of();
        }

        List<SelectionRecord> records = this.list(
                new LambdaQueryWrapper<SelectionRecord>()
                        .in(SelectionRecord::getTopicId, topicIds)
                        .orderByAsc(SelectionRecord::getPriority)
        );

        return toVOList(records);
    }

    @Override
    @Transactional
    public void teacherReview(TeacherReviewDTO dto) {
        SelectionRecord record = this.getById(dto.getId());

        if (record == null) {
            throw new BusinessException("志愿记录不存在");
        }

        // 验证该教师是课题的创建者
        Topic topic = topicMapper.selectById(record.getTopicId());
        if (topic == null || !topic.getCreatorId().equals(UserContext.getUserId())) {
            throw new BusinessException("无权审核该志愿");
        }

        record.setTeacherAction(dto.getAction());
        record.setTeacherComment(dto.getComment());
        record.setUpdatedAt(LocalDateTime.now());
        this.updateById(record);

        if ("approve".equals(dto.getAction())) {
            record.setIsSelected((byte) 1);
            this.updateById(record);

            // 检查该学生是否已有其他已通过的志愿，如果有则拒绝
            List<SelectionRecord> otherRecords = this.list(
                    new LambdaQueryWrapper<SelectionRecord>()
                            .eq(SelectionRecord::getStudentId, record.getStudentId())
                            .eq(SelectionRecord::getBatchId, record.getBatchId())
                            .ne(SelectionRecord::getId, record.getId())
            );

            for (SelectionRecord other : otherRecords) {
                other.setTeacherAction("reject");
                other.setTeacherComment("学生已被其他课题录取");
                other.setUpdatedAt(LocalDateTime.now());
                this.updateById(other);
            }

            // 创建 StudentTopic 记录
            StudentTopic st = new StudentTopic();
            st.setBatchId(record.getBatchId());
            st.setStudentId(record.getStudentId());
            st.setTopicId(record.getTopicId());
            st.setAdvisorId(topic.getCreatorId());
            st.setStatus("active");
            st.setAllocationTime(LocalDateTime.now());
            studentTopicMapper.insert(st);

            // 更新课题已选人数
            topic.setCurrentCount((topic.getCurrentCount() == null ? 0 : topic.getCurrentCount()) + 1);
            topicMapper.updateById(topic);

            log.info("教师[{}]通过学生[{}]的志愿，课题[{}]", UserContext.getUserId(), record.getStudentId(), record.getTopicId());
        } else {
            log.info("教师[{}]拒绝学生[{}]的志愿", UserContext.getUserId(), record.getStudentId());
        }
    }

    @Override
    @Transactional
    public void autoAllocate(Long batchId) {
        log.info("开始系统自动分配，批次[{}]", batchId);

        Batch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        // 查找未选中的学生
        List<SelectionRecord> unselected = this.list(
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(SelectionRecord::getBatchId, batchId)
                        .eq(SelectionRecord::getIsSelected, 0)
                        .isNull(SelectionRecord::getTeacherAction)
        );

        if (unselected.isEmpty()) {
            log.info("没有需要分配的学生");
            return;
        }

        // 按优先级分配
        Map<Long, List<SelectionRecord>> byStudent = unselected.stream()
                .collect(Collectors.groupingBy(SelectionRecord::getStudentId));

        for (Map.Entry<Long, List<SelectionRecord>> entry : byStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<SelectionRecord> prefs = entry.getValue();
            prefs.sort((a, b) -> Byte.compare(a.getPriority(), b.getPriority()));

            boolean allocated = false;
            for (SelectionRecord pref : prefs) {
                Topic topic = topicMapper.selectById(pref.getTopicId());

                if (topic.getMaxCapacity() != null && topic.getMaxCapacity() > 0 &&
                        topic.getCurrentCount() != null && topic.getCurrentCount() >= topic.getMaxCapacity()) {
                    continue;
                }

                pref.setTeacherAction("approve");
                pref.setTeacherComment("系统自动分配");
                pref.setIsSelected((byte) 1);
                pref.setUpdatedAt(LocalDateTime.now());
                this.updateById(pref);

                StudentTopic st = new StudentTopic();
                st.setBatchId(batchId);
                st.setStudentId(studentId);
                st.setTopicId(pref.getTopicId());
                st.setAdvisorId(topic.getCreatorId());
                st.setStatus("active");
                st.setAllocationTime(LocalDateTime.now());
                studentTopicMapper.insert(st);

                topic.setCurrentCount((topic.getCurrentCount() == null ? 0 : topic.getCurrentCount()) + 1);
                topicMapper.updateById(topic);

                allocated = true;
                break;
            }

            if (!allocated) {
                log.warn("学生[{}]未分配到任何课题", studentId);
            }
        }

        log.info("系统自动分配完成，批次[{}]", batchId);
    }

    private List<SelectionRecordVO> toVOList(List<SelectionRecord> records) {
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long, Topic> topicMap = topicMapper.selectList(null).stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));

        return records.stream().map(r -> {
            SelectionRecordVO vo = new SelectionRecordVO();
            vo.setId(r.getId());
            vo.setBatchId(r.getBatchId());
            vo.setStudentId(r.getStudentId());
            vo.setStudentName(userMap.getOrDefault(r.getStudentId(), ""));
            vo.setTopicId(r.getTopicId());
            Topic t = topicMap.get(r.getTopicId());
            if (t != null) {
                vo.setTopicTitle(t.getTitle());
                vo.setAdvisorId(t.getCreatorId());
                vo.setAdvisorName(userMap.getOrDefault(t.getCreatorId(), ""));
            }
            vo.setPriority(r.getPriority() != null ? r.getPriority().intValue() : null);
            vo.setTeacherAction(r.getTeacherAction());
            vo.setTeacherComment(r.getTeacherComment());
            vo.setIsSelected(r.getIsSelected() != null ? r.getIsSelected().intValue() : 0);
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
