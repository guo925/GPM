package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.MajorCreateDTO;
import com.gjx.gpms.entity.Major;
import com.gjx.gpms.vo.MajorVO;

import java.util.List;

/**
 * 专业服务接口
 *
 * @author gpms
 */
public interface MajorService extends IService<Major> {

    List<MajorVO> listAll(Long collegeId);

    void create(MajorCreateDTO dto);

    void update(Long id, MajorCreateDTO dto);

    void deleteById(Long id);
}
