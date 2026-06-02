package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.CollegeCreateDTO;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.mapper.CollegeMapper;
import com.gjx.gpms.service.CollegeService;
import com.gjx.gpms.vo.CollegeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学院服务实现
 *
 * @author gpms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    /**
     * 查询列表all相关逻辑。
     */
    @Override
    public List<CollegeVO> listAll() {
        List<College> list = this.list(
                new LambdaQueryWrapper<College>().orderByAsc(College::getSortOrder)
        );

        return list.stream().map(e -> {
            CollegeVO vo = new CollegeVO();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 创建相关逻辑。
     */
    @Override
    public void create(CollegeCreateDTO dto) {
        log.info("新增学院：{}", dto.getCode());

        Long count = this.count(
                new LambdaQueryWrapper<College>().eq(College::getCode, dto.getCode())
        );

        if (count > 0) {
            throw new BusinessException("学院代码已存在");
        }

        College entity = new College();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);

        log.info("新增学院成功：{}", dto.getCode());
    }

    /**
     * 更新相关逻辑。
     */
    @Override
    public void update(Long id, CollegeCreateDTO dto) {
        log.info("修改学院：{}", id);

        College entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("学院不存在");
        }

        Long count = this.count(
                new LambdaQueryWrapper<College>()
                        .eq(College::getCode, dto.getCode())
                        .ne(College::getId, id)
        );

        if (count > 0) {
            throw new BusinessException("学院代码已存在");
        }

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setSortOrder(dto.getSortOrder());
        this.updateById(entity);

        log.info("修改学院成功：{}", id);
    }

    /**
     * 删除by id相关逻辑。
     */
    @Override
    public void deleteById(Long id) {
        College entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("学院不存在");
        }
        this.removeById(id);
        log.info("删除学院成功：{}", id);
    }
}
