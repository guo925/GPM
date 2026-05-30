package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.GuidanceRecordCreateDTO;
import com.gjx.gpms.entity.GuidanceRecord;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.GuidanceRecordMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.GuidanceRecordService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.GuidanceRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指导记录服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuidanceRecordServiceImpl extends ServiceImpl<GuidanceRecordMapper, GuidanceRecord> implements GuidanceRecordService {

    private final StudentTopicMapper studentTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    @Override
    public void create(GuidanceRecordCreateDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("学生[{}]提交第{}周指导记录", userId, dto.getWeekNumber());

        StudentTopic st = studentTopicMapper.selectById(dto.getStudentTopicId());
        if (st == null) {
            throw new BusinessException("选题记录不存在");
        }

        GuidanceRecord record = new GuidanceRecord();
        record.setStudentTopicId(dto.getStudentTopicId());
        record.setWeekNumber(dto.getWeekNumber());
        record.setContent(dto.getContent());
        record.setFilePath(dto.getFilePath());
        record.setStatus("submitted");

        this.save(record);
        log.info("指导记录提交成功");
    }

    @Override
    public void review(Long id, String comment) {
        log.info("审核指导记录[{}]", id);

        GuidanceRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("指导记录不存在");
        }

        record.setStatus("reviewed");
        record.setAdvisorComment(comment);
        record.setReviewedAt(LocalDateTime.now());

        this.updateById(record);
        log.info("指导记录审核完成");
    }

    @Override
    public List<GuidanceRecordVO> getByStudentTopic(Long studentTopicId) {
        List<GuidanceRecord> records = this.list(
                new LambdaQueryWrapper<GuidanceRecord>()
                        .eq(GuidanceRecord::getStudentTopicId, studentTopicId)
                        .orderByAsc(GuidanceRecord::getWeekNumber)
        );

        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        StudentTopic st = studentTopicMapper.selectById(studentTopicId);
        String studentName = "";
        String topicTitle = "";
        if (st != null) {
            studentName = userMap.getOrDefault(st.getStudentId(), "");
            Topic topic = topicMapper.selectById(st.getTopicId());
            if (topic != null) {
                topicTitle = topic.getTitle();
            }
        }

        String finalStudentName = studentName;
        String finalTopicTitle = topicTitle;

        return records.stream().map(r -> {
            GuidanceRecordVO vo = new GuidanceRecordVO();
            vo.setId(r.getId());
            vo.setStudentTopicId(r.getStudentTopicId());
            vo.setStudentName(finalStudentName);
            vo.setTopicTitle(finalTopicTitle);
            vo.setWeekNumber(r.getWeekNumber());
            vo.setContent(r.getContent());
            vo.setFilePath(r.getFilePath());
            vo.setStatus(r.getStatus());
            vo.setAdvisorComment(r.getAdvisorComment());
            vo.setReviewedAt(r.getReviewedAt());
            vo.setCreatedAt(r.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
