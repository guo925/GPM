package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.MajorCreateDTO;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.entity.Major;
import com.gjx.gpms.mapper.CollegeMapper;
import com.gjx.gpms.mapper.MajorMapper;
import com.gjx.gpms.service.MajorService;
import com.gjx.gpms.vo.MajorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专业服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final CollegeMapper collegeMapper;

    @Override
    public List<MajorVO> listAll(Long collegeId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (collegeId != null) {
            wrapper.eq(Major::getCollegeId, collegeId);
        }
        wrapper.orderByAsc(Major::getSortOrder);

        List<Major> list = this.list(wrapper);

        Map<Long, String> collegeMap = collegeMapper.selectList(null).stream()
                .collect(Collectors.toMap(College::getId, College::getName));

        return list.stream().map(e -> {
            MajorVO vo = new MajorVO();
            BeanUtils.copyProperties(e, vo);
            vo.setCollegeName(collegeMap.getOrDefault(e.getCollegeId(), ""));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void create(MajorCreateDTO dto) {
        log.info("新增专业：{}", dto.getCode());

        Long count = this.count(
                new LambdaQueryWrapper<Major>().eq(Major::getCode, dto.getCode())
        );

        if (count > 0) {
            throw new BusinessException("专业代码已存在");
        }

        Major entity = new Major();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);

        log.info("新增专业成功：{}", dto.getCode());
    }

    @Override
    public void update(Long id, MajorCreateDTO dto) {
        log.info("修改专业：{}", id);

        Major entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("专业不存在");
        }

        Long count = this.count(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getCode, dto.getCode())
                        .ne(Major::getId, id)
        );

        if (count > 0) {
            throw new BusinessException("专业代码已存在");
        }

        entity.setCollegeId(dto.getCollegeId());
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setSortOrder(dto.getSortOrder());
        this.updateById(entity);

        log.info("修改专业成功：{}", id);
    }

    @Override
    public void deleteById(Long id) {
        Major entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("专业不存在");
        }
        this.removeById(id);
        log.info("删除专业成功：{}", id);
    }
}
