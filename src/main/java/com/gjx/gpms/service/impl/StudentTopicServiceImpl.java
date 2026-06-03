package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.entity.Topic;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mapper.StudentTopicMapper;
import com.gjx.gpms.mapper.TopicMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.service.BatchService;
import com.gjx.gpms.service.StudentTopicService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.StudentTopicVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生选题结果服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentTopicServiceImpl extends ServiceImpl<StudentTopicMapper, StudentTopic> implements StudentTopicService {

    private final BatchMapper batchMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final BatchService batchService;

    /**
     * 分页查询相关逻辑。
     */
    @Override
    public IPage<StudentTopicVO> page(long current, long size, Long batchId, String grade, Long advisorId) {
        Page<StudentTopic> page = new Page<>(current, size);
        List<Long> batchIds = resolveBatchIds(batchId, grade);

        LambdaQueryWrapper<StudentTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(batchIds != null && !batchIds.isEmpty(), StudentTopic::getBatchId, batchIds);
        wrapper.eq(advisorId != null, StudentTopic::getAdvisorId, advisorId);
        applyUserScope(wrapper);
        wrapper.orderByDesc(StudentTopic::getCreatedAt);

        Page<StudentTopic> stPage = this.page(page, wrapper);

        Map<Long, String> batchMap = batchMapper.selectList(null).stream()
                .collect(Collectors.toMap(Batch::getId, Batch::getName));
        Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        Map<Long, Topic> topicMap = topicMapper.selectList(null).stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));

        Page<StudentTopicVO> voPage = new Page<>();
        voPage.setCurrent(stPage.getCurrent());
        voPage.setSize(stPage.getSize());
        voPage.setTotal(stPage.getTotal());
        voPage.setRecords(stPage.getRecords().stream().map(st -> {
            StudentTopicVO vo = new StudentTopicVO();
            vo.setId(st.getId());
            vo.setBatchId(st.getBatchId());
            vo.setBatchName(batchMap.getOrDefault(st.getBatchId(), ""));
            vo.setStudentId(st.getStudentId());
            vo.setStudentName(userMap.getOrDefault(st.getStudentId(), ""));
            vo.setTopicId(st.getTopicId());
            Topic t = topicMap.get(st.getTopicId());
            if (t != null) {
                vo.setTopicTitle(t.getTitle());
            }
            vo.setAdvisorId(st.getAdvisorId());
            vo.setAdvisorName(userMap.getOrDefault(st.getAdvisorId(), ""));
            vo.setStatus(st.getStatus());
            vo.setAllocationTime(st.getAllocationTime());
            vo.setCreatedAt(st.getCreatedAt());
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    private void applyUserScope(LambdaQueryWrapper<StudentTopic> wrapper) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null || loginUser.getRoleCodes() == null) {
            return;
        }
        if (loginUser.getRoleCodes().contains("STUDENT")) {
            wrapper.eq(StudentTopic::getStudentId, loginUser.getUserId());
        } else if (loginUser.getRoleCodes().contains("TEACHER")) {
            wrapper.eq(StudentTopic::getAdvisorId, loginUser.getUserId());
        }
    }

    /**
     * 获取ByStudentId。
     */
    @Override
    public StudentTopicVO getByStudentId(Long studentId) {
        if (studentId == null) return null;

        StudentTopic st = this.getOne(
                new LambdaQueryWrapper<StudentTopic>()
                        .eq(StudentTopic::getStudentId, studentId)
                        .eq(StudentTopic::getStatus, "active")
                        .orderByDesc(StudentTopic::getAllocationTime)
                        .orderByDesc(StudentTopic::getCreatedAt)
                        .last("LIMIT 1")
        );

        if (st == null) {
            return null;
        }

        Batch batch = batchMapper.selectById(st.getBatchId());
        Topic topic = topicMapper.selectById(st.getTopicId());
        User student = userMapper.selectById(st.getStudentId());
        User advisor = userMapper.selectById(st.getAdvisorId());

        StudentTopicVO vo = new StudentTopicVO();
        vo.setId(st.getId());
        vo.setBatchId(st.getBatchId());
        vo.setBatchName(batch != null ? batch.getName() : "");
        vo.setStudentId(st.getStudentId());
        vo.setStudentName(student != null ? student.getRealName() : "");
        vo.setTopicId(st.getTopicId());
        vo.setTopicTitle(topic != null ? topic.getTitle() : "");
        vo.setAdvisorId(st.getAdvisorId());
        vo.setAdvisorName(advisor != null ? advisor.getRealName() : "");
        vo.setStatus(st.getStatus());
        vo.setAllocationTime(st.getAllocationTime());
        vo.setCreatedAt(st.getCreatedAt());
        return vo;
    }

    private List<Long> resolveBatchIds(Long batchId, String grade) {
        if (grade != null && !grade.isBlank()) {
            return batchService.resolveBatchIdsByGrade(grade);
        }
        return batchId != null ? List.of(batchId) : null;
    }
}
