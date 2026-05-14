package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.ProcessReviewDTO;
import com.gjx.gpms.dto.ProcessSubmitDTO;
import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.ProcessInstanceMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.ProcessInstanceService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.ProcessInstanceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现（状态机核心）
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl extends ServiceImpl<ProcessInstanceMapper, ProcessInstance> implements ProcessInstanceService {

    private final StudentTopicMapper studentTopicMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    @Override
    public void submit(ProcessSubmitDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("用户[{}]提交阶段[{}]", userId, dto.getStage());

        StudentTopic st = studentTopicMapper.selectById(dto.getStudentTopicId());
        if (st == null) {
            throw new BusinessException("选题记录不存在");
        }

        // 验证提交人身份（学生本人或导师）
        if (!userId.equals(st.getStudentId()) && !userId.equals(st.getAdvisorId())) {
            throw new BusinessException("无权提交");
        }

        // 查找已有记录
        ProcessInstance existing = this.getOne(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, dto.getStudentTopicId())
                        .eq(ProcessInstance::getStage, dto.getStage())
        );

        if (existing != null) {
            if ("approved".equals(existing.getStatus())) {
                throw new BusinessException("该阶段已通过，请勿重复提交");
            }

            // 驳回后重新提交，版本号+1
            existing.setContent(dto.getContent());
            existing.setFilePath(dto.getFilePath());
            existing.setStatus("submitted");
            existing.setSubmittedAt(LocalDateTime.now());
            existing.setVersion(existing.getVersion() + 1);
            existing.setSubmitterId(userId);
            this.updateById(existing);
            log.info("阶段[{}]重新提交，版本号：{}", dto.getStage(), existing.getVersion());
            return;
        }

        // 新建流程实例
        ProcessInstance pi = new ProcessInstance();
        pi.setStudentTopicId(dto.getStudentTopicId());
        pi.setStage(dto.getStage());
        pi.setStatus("submitted");
        pi.setSubmitterId(userId);
        pi.setSubmittedAt(LocalDateTime.now());
        pi.setContent(dto.getContent());
        pi.setFilePath(dto.getFilePath());
        pi.setVersion(1);
        pi.setIsEditable((byte) 0);

        this.save(pi);
        log.info("阶段[{}]首次提交", dto.getStage());
    }

    @Override
    public void review(ProcessReviewDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("用户[{}]审核流程实例[{}]：{}", userId, dto.getId(), dto.getStatus());

        ProcessInstance pi = this.getById(dto.getId());
        if (pi == null) {
            throw new BusinessException("流程实例不存在");
        }

        if (!"submitted".equals(pi.getStatus())) {
            throw new BusinessException("当前状态不可审核");
        }

        pi.setStatus(dto.getStatus());
        pi.setReviewerId(userId);
        pi.setReviewedAt(LocalDateTime.now());
        pi.setReviewComment(dto.getReviewComment());

        if ("approved".equals(dto.getStatus())) {
            pi.setIsEditable((byte) 0);
        } else {
            // 驳回后可修改
            pi.setIsEditable((byte) 1);
        }

        this.updateById(pi);
        log.info("流程实例[{}]审核完成：{}", dto.getId(), dto.getStatus());
    }

    @Override
    public List<ProcessInstanceVO> getByStudentTopic(Long studentTopicId) {
        List<ProcessInstance> list = this.list(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopicId)
                        .orderByAsc(ProcessInstance::getStage)
        );

        return toVOList(list);
    }

    @Override
    public ProcessInstanceVO getCurrentStage(Long studentTopicId, String stage) {
        ProcessInstance pi = this.getOne(
                new LambdaQueryWrapper<ProcessInstance>()
                        .eq(ProcessInstance::getStudentTopicId, studentTopicId)
                        .eq(ProcessInstance::getStage, stage)
        );

        if (pi == null) {
            ProcessInstanceVO vo = new ProcessInstanceVO();
            vo.setStudentTopicId(studentTopicId);
            vo.setStage(stage);
            vo.setStatus("not_started");
            return vo;
        }

        return toVO(pi);
    }

    private ProcessInstanceVO toVO(ProcessInstance pi) {
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));

        StudentTopic st = studentTopicMapper.selectById(pi.getStudentTopicId());
        String studentName = "";
        String topicTitle = "";
        if (st != null) {
            studentName = userMap.getOrDefault(st.getStudentId(), "");
            Topic topic = topicMapper.selectById(st.getTopicId());
            if (topic != null) {
                topicTitle = topic.getTitle();
            }
        }

        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(pi.getId());
        vo.setStudentTopicId(pi.getStudentTopicId());
        vo.setStudentName(studentName);
        vo.setTopicTitle(topicTitle);
        vo.setStage(pi.getStage());
        vo.setStatus(pi.getStatus());
        vo.setSubmitterName(userMap.getOrDefault(pi.getSubmitterId(), ""));
        vo.setSubmittedAt(pi.getSubmittedAt());
        vo.setContent(pi.getContent());
        vo.setFilePath(pi.getFilePath());
        vo.setReviewerName(userMap.getOrDefault(pi.getReviewerId(), ""));
        vo.setReviewedAt(pi.getReviewedAt());
        vo.setReviewComment(pi.getReviewComment());
        vo.setVersion(pi.getVersion());
        vo.setCreatedAt(pi.getCreatedAt());
        return vo;
    }

    private List<ProcessInstanceVO> toVOList(List<ProcessInstance> list) {
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
}
