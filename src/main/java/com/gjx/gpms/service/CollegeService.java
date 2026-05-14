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

    List<CollegeVO> listAll();

    void create(CollegeCreateDTO dto);

    void update(Long id, CollegeCreateDTO dto);

    void deleteById(Long id);
}
