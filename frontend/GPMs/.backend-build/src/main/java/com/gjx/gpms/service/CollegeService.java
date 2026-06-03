package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.CollegeCreateDTO;
import com.gjx.gpms.entity.College;
import com.gjx.gpms.vo.CollegeVO;

import java.util.List;

/**
 * 学院服务接口
 *
 * @author gpms
 */
public interface CollegeService extends IService<College> {

    /**
     * 查询列表all相关逻辑。
     */
    List<CollegeVO> listAll();

    /**
     * 创建相关逻辑。
     */
    void create(CollegeCreateDTO dto);

    /**
     * 更新相关逻辑。
     */
    void update(Long id, CollegeCreateDTO dto);

    /**
     * 删除by id相关逻辑。
     */
    void deleteById(Long id);
}
