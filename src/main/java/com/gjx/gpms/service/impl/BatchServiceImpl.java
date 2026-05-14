package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.BatchCreateDTO;
import com.gjx.gpms.dto.BatchUpdateDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.entity.Major;
import com.gjx.gpms.mapper.BatchMapper;
import com.gjx.gpms.mapper.CollegeMapper;
import com.gjx.gpms.mapper.MajorMapper;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.BatchService;
import com.gjx.gpms.vo.BatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
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

    @Override
    public IPage<BatchVO> page(long current, long size, String name, Integer status) {
        Page<Batch> page = new Page<>(current, size);

        LambdaQueryWrapper<Batch> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Batch::getName, name);
        wrapper.eq(status != null, Batch::getStatus, status);
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
            vo.setCollegeName(collegeMap.getOrDefault(b.getCollegeId(), ""));
            vo.setMajorName(majorMap.getOrDefault(b.getMajorId(), ""));
            vo.setCurrentStage(b.getCurrentStage());
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public BatchVO getDetail(Long id) {
        Batch b = this.getById(id);
        if (b == null) {
            throw new BusinessException("批次不存在");
        }

        College college = collegeMapper.selectById(b.getCollegeId());
        Major major = majorMapper.selectById(b.getMajorId());

        BatchVO vo = new BatchVO();
        BeanUtils.copyProperties(b, vo);
        vo.setCollegeName(college != null ? college.getName() : "");
        vo.setMajorName(major != null ? major.getName() : "");
        return vo;
    }

    @Override
    public void create(BatchCreateDTO dto) {
        log.info("新增批次：{}", dto.getName());

        Batch entity = new Batch();
        BeanUtils.copyProperties(dto, entity);
        entity.setCurrentStage("topic_selection");
        entity.setStatus((byte) 1);
        entity.setCreatedBy(UserContext.getUserId());

        this.save(entity);

        log.info("新增批次成功：{}", dto.getName());
    }

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

        log.info("修改批次成功：{}", dto.getId());
    }

    @Override
    public void deleteById(Long id) {
        Batch entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("批次不存在");
        }
        this.removeById(id);
        log.info("删除批次成功：{}", id);
    }

    @Override
    public void advanceStage(Long id, String nextStage) {
        Batch entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("批次不存在");
        }
        entity.setCurrentStage(nextStage);
        this.updateById(entity);
        log.info("批次[{}]阶段推进至：{}", id, nextStage);
    }
}
