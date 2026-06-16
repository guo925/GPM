package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.SelectionSubmitDTO;
import com.gjx.gpms.dto.TeacherReviewDTO;
import com.gjx.gpms.entity.*;
import com.gjx.gpms.mapper.*;
import com.gjx.gpms.mq.dto.SelectionSubmitMessage;
import com.gjx.gpms.mq.producer.MqProducer;
import com.gjx.gpms.mq.producer.NoticeProducer;
import com.gjx.gpms.mq.producer.OperationLogProducer;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.BatchService;
import com.gjx.gpms.service.SelectionRecordService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.SelectionRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final RedisCacheService redisCacheService;
    private final StringRedisTemplate stringRedisTemplate;
    private final MqProducer mqProducer;
    private final NoticeProducer noticeProducer;
    private final OperationLogProducer operationLogProducer;
    private final JdbcTemplate jdbcTemplate;
    private final BatchService batchService;

    /**
     * 提交preferences相关逻辑。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPreferences(SelectionSubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        log.info("学生[{}]提交选题志愿，批次[{}]", studentId, dto.getBatchId());

        Batch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        validateStudentBatchScope(studentId, batch);

        Long activeTopicCount = studentTopicMapper.selectCount(
                new LambdaQueryWrapper<StudentTopic>()
                        .eq(StudentTopic::getBatchId, dto.getBatchId())
                        .eq(StudentTopic::getStudentId, studentId)
                        .eq(StudentTopic::getStatus, "active")
        );
        if (activeTopicCount > 0) {
            throw new BusinessException("已完成选题，不可重复提交志愿");
        }

        List<SelectionRecord> existingRecords = this.list(
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(SelectionRecord::getBatchId, dto.getBatchId())
                        .eq(SelectionRecord::getStudentId, studentId)
        );
        boolean hasApprovedSelection = existingRecords.stream()
                .anyMatch(record -> Byte.valueOf((byte) 1).equals(record.getIsSelected())
                        || "approve".equals(record.getTeacherAction()));
        if (hasApprovedSelection) {
            throw new BusinessException("志愿已通过审核，不可重复提交");
        }

        // 检查志愿数是否超出限制
        if (dto.getTopicIds().size() > batch.getStudentMaxChoices()) {
            throw new BusinessException("最多选择" + batch.getStudentMaxChoices() + "个志愿");
        }

        // 提交前校验课题有效性，避免无效消息进入 MQ。
        for (Long topicId : dto.getTopicIds()) {
            Topic topic = topicMapper.selectById(topicId);
            if (topic == null || !"approved".equals(topic.getStatus())) {
                throw new BusinessException("课题[" + topicId + "]不存在或未审核通过");
            }
            if (!dto.getBatchId().equals(topic.getBatchId())) {
                throw new BusinessException("课题[" + topicId + "]不属于当前批次");
            }
        }

        Long reservedTopicId = null;
        if ("first_come".equals(batch.getSelectionMode())) {
            reservedTopicId = dto.getTopicIds().get(0);
            preDeductQuota(dto.getBatchId(), studentId, reservedTopicId);
        }

        SelectionSubmitMessage message = new SelectionSubmitMessage();
        message.setMessageId(UUID.randomUUID().toString());
        message.setBatchId(dto.getBatchId());
        message.setStudentId(studentId);
        message.setTopicIds(dto.getTopicIds());
        message.setReservedTopicId(reservedTopicId);
        message.setSubmittedAt(LocalDateTime.now());

        if (!existingRecords.isEmpty()) {
            this.remove(
                    new LambdaQueryWrapper<SelectionRecord>()
                            .eq(SelectionRecord::getBatchId, dto.getBatchId())
                            .eq(SelectionRecord::getStudentId, studentId)
            );
            saveSelectionRecords(dto.getBatchId(), studentId, dto.getTopicIds());
            log.info("学生[{}]重新提交选题志愿成功，批次[{}]，共{}个志愿",
                    studentId, dto.getBatchId(), dto.getTopicIds().size());
            return;
        }

        try {
            mqProducer.sendSelection(message);
        } catch (AmqpException e) {
            log.warn("RabbitMQ不可用，选题志愿改为同步落库，studentId={}, batchId={}: {}",
                    studentId, dto.getBatchId(), e.getMessage());
            saveSelectionRecords(dto.getBatchId(), studentId, dto.getTopicIds());
            return;
        }

        log.info("学生[{}]提交选题请求已进入异步队列，共{}个志愿", studentId, dto.getTopicIds().size());
    }

    private void validateStudentBatchScope(Long studentId, Batch batch) {
        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        if (student.getCollegeId() == null || student.getMajorId() == null || student.getGrade() == null || student.getGrade().isBlank()) {
            throw new BusinessException("学生年级、学院、专业信息不完整，无法提交选题");
        }
        if (!student.getCollegeId().equals(batch.getCollegeId())
                || !student.getMajorId().equals(batch.getMajorId())
                || !student.getGrade().equals(batch.getGrade())) {
            throw new BusinessException("只能提交本人年级、学院、专业对应批次的选题志愿");
        }
    }

    private void saveSelectionRecords(Long batchId, Long studentId, List<Long> topicIds) {
        Long existCount = this.count(
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(SelectionRecord::getBatchId, batchId)
                        .eq(SelectionRecord::getStudentId, studentId)
        );
        if (existCount > 0) {
            throw new BusinessException("已提交过志愿，请勿重复提交");
        }

        for (int i = 0; i < topicIds.size(); i++) {
            SelectionRecord record = new SelectionRecord();
            record.setBatchId(batchId);
            record.setStudentId(studentId);
            record.setTopicId(topicIds.get(i));
            record.setPriority((byte) (i + 1));
            record.setIsSelected((byte) 0);
            record.setCreatedAt(LocalDateTime.now());
            this.save(record);
            syncSelectionCurrent(record);
        }
        log.info("学生[{}]选题志愿同步落库成功，批次[{}]，共{}个志愿", studentId, batchId, topicIds.size());
    }

    /**
     * 获取MySelections。
     */
    @Override
    public List<SelectionRecordVO> getMySelections(Long batchId, String grade) {
        Long studentId = UserContext.getUserId();
        List<Long> batchIds = resolveBatchIds(batchId, grade);

        List<SelectionRecord> records = this.list(
                new LambdaQueryWrapper<SelectionRecord>()
                        .in(batchIds != null && !batchIds.isEmpty(), SelectionRecord::getBatchId, batchIds)
                        .eq(SelectionRecord::getStudentId, studentId)
                        .orderByAsc(SelectionRecord::getPriority)
        );

        return toVOList(records);
    }

    /**
     * 获取TeacherReviewList。
     */
    @Override
    public List<SelectionRecordVO> getTeacherReviewList(Long batchId, String grade) {
        Long teacherId = UserContext.getUserId();
        List<Long> batchIds = resolveBatchIds(batchId, grade);

        if (isAdminUser()) {
            List<SelectionRecord> records = this.list(
                    new LambdaQueryWrapper<SelectionRecord>()
                            .in(batchIds != null && !batchIds.isEmpty(), SelectionRecord::getBatchId, batchIds)
                            .orderByAsc(SelectionRecord::getPriority)
            );
            return toVOList(records);
        }

        // 查找该教师创建的课题
        List<Topic> myTopics = topicMapper.selectList(
                new LambdaQueryWrapper<Topic>()
                        .in(batchIds != null && !batchIds.isEmpty(), Topic::getBatchId, batchIds)
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

    private boolean isAdminUser() {
        if (UserContext.getLoginUser() == null || UserContext.getLoginUser().getRoleCodes() == null) {
            return false;
        }
        return UserContext.getLoginUser().getRoleCodes().stream()
                .anyMatch(role -> role.endsWith("_ADMIN") || "SUPER_ADMIN".equals(role));
    }

    /**
     * 处理teacherReview相关逻辑。
     */
    @Override
    @Transactional
    public void teacherReview(TeacherReviewDTO dto) {
        SelectionRecord record = this.getById(dto.getId());

        if (record == null) {
            throw new BusinessException("志愿记录不存在");
        }

        // 教师只能审核自己创建课题的志愿，管理员可以审核全部志愿。
        Topic topic = topicMapper.selectById(record.getTopicId());
        if (topic == null || (!isAdminUser() && !topic.getCreatorId().equals(UserContext.getUserId()))) {
            throw new BusinessException("无权审核该志愿");
        }

        record.setTeacherAction(dto.getAction());
        record.setTeacherComment(dto.getComment());
        record.setUpdatedAt(LocalDateTime.now());
        this.updateById(record);
        syncSelectionCurrent(record);

        if ("approve".equals(dto.getAction())) {
            record.setIsSelected((byte) 1);
            this.updateById(record);
            syncSelectionCurrent(record);

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
                syncSelectionCurrent(other);
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
            syncTopicCurrent(topic);
            noticeProducer.send(record.getStudentId(), "选题审核通过", "你的选题志愿已通过审核", "selection");
            operationLogProducer.send(UserContext.getUserId(), "AUDIT", "selection_record", String.valueOf(record.getId()), "教师通过选题志愿");

            log.info("教师[{}]通过学生[{}]的志愿，课题[{}]", UserContext.getUserId(), record.getStudentId(), record.getTopicId());
        } else {
            noticeProducer.send(record.getStudentId(), "选题审核未通过", "你的选题志愿未通过审核", "selection");
            operationLogProducer.send(UserContext.getUserId(), "AUDIT", "selection_record", String.valueOf(record.getId()), "教师拒绝选题志愿");
            log.info("教师[{}]拒绝学生[{}]的志愿", UserContext.getUserId(), record.getStudentId());
        }
    }

    /**
     * 处理autoAllocate相关逻辑。
     */
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
                syncSelectionCurrent(pref);

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
                syncTopicCurrent(topic);
                noticeProducer.send(studentId, "选题分配成功", "系统已为你分配课题", "selection");

                allocated = true;
                break;
            }

            if (!allocated) {
                log.warn("学生[{}]未分配到任何课题", studentId);
            }
        }

        log.info("系统自动分配完成，批次[{}]", batchId);
    }

    /**
     * 处理preDeductQuota相关逻辑。
     */
    private void preDeductQuota(Long batchId, Long studentId, Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        int remain = Math.max(0, (topic.getMaxCapacity() == null ? 0 : topic.getMaxCapacity())
                - (topic.getCurrentCount() == null ? 0 : topic.getCurrentCount()));
        String quotaKey = CacheKeys.selectionQuota(topicId);
        if (stringRedisTemplate.opsForValue().get(quotaKey) == null) {
            stringRedisTemplate.opsForValue().set(quotaKey, String.valueOf(remain), Duration.ofHours(2));
        }

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/selection_quota_decrement.lua"));
        script.setResultType(Long.class);
        Long result = stringRedisTemplate.execute(
                script,
                Arrays.asList(quotaKey, CacheKeys.selectionStudent(batchId, studentId)),
                String.valueOf(Duration.ofHours(2).toSeconds())
        );
        if (result == null || result == -1L) {
            throw new BusinessException("课题名额未初始化，请稍后重试");
        }
        if (result == -2L) {
            throw new BusinessException("已提交过志愿，请勿重复提交");
        }
        if (result == 0L) {
            throw new BusinessException("课题名额已满");
        }
    }

    /**
     * 转换volist相关逻辑。
     */
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

    /**
     * 同步selection current相关逻辑。
     */
    private void syncSelectionCurrent(SelectionRecord record) {
        try {
            jdbcTemplate.update("""
                    REPLACE INTO selection_current
                    (id,batch_id,student_id,topic_id,priority,teacher_action,teacher_comment,is_selected,created_at,updated_at)
                    VALUES (?,?,?,?,?,?,?,?,?,?)
                    """,
                    record.getId(),
                    record.getBatchId(),
                    record.getStudentId(),
                    record.getTopicId(),
                    record.getPriority(),
                    record.getTeacherAction(),
                    record.getTeacherComment(),
                    record.getIsSelected(),
                    record.getCreatedAt(),
                    record.getUpdatedAt()
            );
        } catch (Exception e) {
            log.debug("selection_current 未就绪，跳过选题热表同步：{}", e.getMessage());
        }
    }

    /**
     * 同步topic current相关逻辑。
     */
    private List<Long> resolveBatchIds(Long batchId, String grade) {
        if (grade != null && !grade.isBlank()) {
            return batchService.resolveBatchIdsByGrade(grade);
        }
        return batchId != null ? List.of(batchId) : null;
    }

    private void syncTopicCurrent(Topic topic) {
        try {
            jdbcTemplate.update("UPDATE topic_current SET current_count=?, updated_at=? WHERE id=?",
                    topic.getCurrentCount(),
                    topic.getUpdatedAt(),
                    topic.getId()
            );
        } catch (Exception e) {
            log.debug("topic_current 未就绪，跳过课题人数同步：{}", e.getMessage());
        }
    }
}
