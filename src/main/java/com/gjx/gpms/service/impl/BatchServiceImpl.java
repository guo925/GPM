package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.cache.CacheKeys;
import com.gjx.gpms.cache.RedisCacheService;
import com.gjx.gpms.dto.BatchCreateDTO;
import com.gjx.gpms.dto.BatchUpdateDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.entity.Major;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mapper.CollegeMapper;
import com.gjx.gpms.mapper.MajorMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.security.model.LoginUser;
import com.gjx.gpms.service.BatchService;
import com.gjx.gpms.system.entity.User;
import com.gjx.gpms.system.mapper.UserMapper;
import com.gjx.gpms.vo.BatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * 批次服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;
    private final RedisCacheService redisCacheService;
    private final UserMapper userMapper;

    /**
     * 分页查询相关逻辑。
     */
    @Override
    public IPage<BatchVO> page(long current, long size, String name, Integer status) {
        Page<Batch> page = new Page<>(current, size);

        LambdaQueryWrapper<Batch> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Batch::getName, name);
        wrapper.eq(status != null, Batch::getStatus, status);
        applyStudentScope(wrapper);
        wrapper.orderByDesc(Batch::getCreatedAt);

        Page<Batch> batchPage = this.page(page, wrapper);

        Map<Long, String> collegeMap = collegeMapper.selectList(null).stream()
                .collect(Collectors.toMap(College::getId, College::getName));
        Map<Long, String> majorMap = majorMapper.selectList(null).stream()
                .collect(Collectors.toMap(Major::getId, Major::getName));

        Page<BatchVO> voPage = new Page<>();
        voPage.setCurrent(batchPage.getCurrent());
        voPage.setSize(batchPage.getSize());
        voPage.setTotal(batchPage.getTotal());
        voPage.setRecords(batchPage.getRecords().stream().map(b -> {
            BatchVO vo = new BatchVO();
            BeanUtils.copyProperties(b, vo);
            fillByteFields(b, vo);
            vo.setCollegeName(collegeMap.getOrDefault(b.getCollegeId(), ""));
            vo.setMajorName(majorMap.getOrDefault(b.getMajorId(), ""));
            vo.setCurrentStage(b.getCurrentStage());
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    private void applyStudentScope(LambdaQueryWrapper<Batch> wrapper) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null
                || loginUser.getRoleCodes() == null
                || !loginUser.getRoleCodes().contains("STUDENT")) {
            return;
        }

        User student = userMapper.selectById(loginUser.getUserId());
        if (student == null || student.getCollegeId() == null || student.getMajorId() == null
                || student.getGrade() == null || student.getGrade().isBlank()) {
            wrapper.apply("1 = 0");
            return;
        }

        wrapper.eq(Batch::getCollegeId, student.getCollegeId())
                .eq(Batch::getMajorId, student.getMajorId())
                .eq(Batch::getGrade, student.getGrade())
                .eq(Batch::getStatus, (byte) 1);
    }

    /**
     * 获取Detail。
     */
    @Override
    public BatchVO getDetail(Long id) {
        Batch b = this.getById(id);
        if (b == null) {
            throw new BusinessException("批次不存在");
        }
        checkStudentBatchAccess(b);

        College college = collegeMapper.selectById(b.getCollegeId());
        Major major = majorMapper.selectById(b.getMajorId());

        BatchVO vo = new BatchVO();
        BeanUtils.copyProperties(b, vo);
        fillByteFields(b, vo);
        vo.setCollegeName(college != null ? college.getName() : "");
        vo.setMajorName(major != null ? major.getName() : "");
        return vo;
    }

    private void checkStudentBatchAccess(Batch batch) {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser == null
                || loginUser.getRoleCodes() == null
                || !loginUser.getRoleCodes().contains("STUDENT")) {
            return;
        }
        User student = userMapper.selectById(loginUser.getUserId());
        if (student == null
                || !batch.getCollegeId().equals(student.getCollegeId())
                || !batch.getMajorId().equals(student.getMajorId())
                || !batch.getGrade().equals(student.getGrade())) {
            throw new BusinessException("无权查看该批次");
        }
    }

    /**
     * 获取CurrentBatch。
     */
    @Override
    public BatchVO getCurrentBatch() {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null && loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("STUDENT")) {
            LambdaQueryWrapper<Batch> wrapper = new LambdaQueryWrapper<Batch>()
                    .eq(Batch::getStatus, (byte) 1);
            applyStudentScope(wrapper);
            Batch current = this.getOne(wrapper.orderByDesc(Batch::getCreatedAt).last("LIMIT 1"));
            return current == null ? null : getDetail(current.getId());
        }
        return redisCacheService.getOrLoad(
                CacheKeys.BATCH_CURRENT,
                BatchVO.class,
                Duration.ofMinutes(30),
                120,
                () -> {
                    Batch current = this.getOne(
                            new LambdaQueryWrapper<Batch>()
                                    .eq(Batch::getStatus, (byte) 1)
                                    .orderByDesc(Batch::getCreatedAt)
                                    .last("LIMIT 1")
                    );
                    if (current == null) {
                        return null;
                    }
                    return getDetail(current.getId());
                }
        );
    }

    /**
     * 创建相关逻辑。
     */
    @Override
    public void create(BatchCreateDTO dto) {
        log.info("新增批次：{}", dto.getName());

        Batch entity = new Batch();
        BeanUtils.copyProperties(dto, entity);
        entity.setCurrentStage("topic_selection");
        entity.setStatus((byte) 1);
        entity.setCreatedBy(UserContext.getUserId());

        this.save(entity);
        redisCacheService.delete(CacheKeys.BATCH_CURRENT);

        log.info("新增批次成功：{}", dto.getName());
    }

    /**
     * 更新相关逻辑。
     */
    @Override
    public void update(BatchUpdateDTO dto) {
        log.info("修改批次：{}", dto.getId());

        Batch entity = this.getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("批次不存在");
        }

        entity.setName(dto.getName());
        if (dto.getConfig() != null) entity.setConfig(dto.getConfig());
        if (dto.getMaxStudentPerTeacher() != null) entity.setMaxStudentPerTeacher(dto.getMaxStudentPerTeacher());
        if (dto.getSelectionMode() != null) entity.setSelectionMode(dto.getSelectionMode());
        if (dto.getStudentMaxChoices() != null) entity.setStudentMaxChoices(dto.getStudentMaxChoices().byteValue());
        if (dto.getAllowTeacherReject() != null) entity.setAllowTeacherReject(dto.getAllowTeacherReject().byteValue());
        if (dto.getRejectStrategy() != null) entity.setRejectStrategy(dto.getRejectStrategy());
        this.updateById(entity);
        redisCacheService.delete(CacheKeys.BATCH_CURRENT);

        log.info("修改批次成功：{}", dto.getId());
    }

    /**
     * 删除by id相关逻辑。
     */
    @Override
    public void deleteById(Long id) {
        Batch entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("批次不存在");
        }
        this.removeById(id);
        redisCacheService.delete(CacheKeys.BATCH_CURRENT);
        log.info("删除批次成功：{}", id);
    }

    /**
     * 处理advanceStage相关逻辑。
     */
    @Override
    public void advanceStage(Long id, String nextStage) {
        Batch entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("批次不存在");
        }
        entity.setCurrentStage(nextStage);
        this.updateById(entity);
        redisCacheService.delete(CacheKeys.BATCH_CURRENT);
        log.info("批次[{}]阶段推进至：{}", id, nextStage);
    }

    private void fillByteFields(Batch batch, BatchVO vo) {
        vo.setStudentMaxChoices(batch.getStudentMaxChoices() == null ? null : batch.getStudentMaxChoices().intValue());
        vo.setAllowTeacherReject(batch.getAllowTeacherReject() == null ? null : batch.getAllowTeacherReject().intValue());
        vo.setStatus(batch.getStatus() == null ? null : batch.getStatus().intValue());
    }

    @Override
    public List<Long> resolveBatchIdsByGrade(String grade) {
        if (grade == null || grade.isBlank()) {
            return Collections.emptyList();
        }
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null && loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("STUDENT")) {
            User student = userMapper.selectById(loginUser.getUserId());
            if (student == null || !grade.equals(student.getGrade())) {
                return Collections.emptyList();
            }
            return this.list(new LambdaQueryWrapper<Batch>()
                    .eq(Batch::getGrade, student.getGrade())
                    .eq(Batch::getCollegeId, student.getCollegeId())
                    .eq(Batch::getMajorId, student.getMajorId())
                    .select(Batch::getId))
                    .stream()
                    .map(Batch::getId)
                    .collect(Collectors.toList());
        }
        return this.list(new LambdaQueryWrapper<Batch>()
                .eq(Batch::getGrade, grade)
                .select(Batch::getId))
                .stream()
                .map(Batch::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getDistinctGrades() {
        LoginUser loginUser = UserContext.getLoginUser();
        if (loginUser != null && loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("STUDENT")) {
            User student = userMapper.selectById(loginUser.getUserId());
            return student != null && student.getGrade() != null && !student.getGrade().isBlank()
                    ? List.of(student.getGrade())
                    : Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<Batch>()
                .select(Batch::getGrade)
                .groupBy(Batch::getGrade)
                .orderByDesc(Batch::getGrade))
                .stream()
                .map(Batch::getGrade)
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
