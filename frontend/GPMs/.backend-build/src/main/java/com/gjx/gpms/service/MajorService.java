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

    /**
     * 查询列表all相关逻辑。
     */
    List<MajorVO> listAll(Long collegeId);

    /**
     * 创建相关逻辑。
     */
    void create(MajorCreateDTO dto);

    /**
     * 更新相关逻辑。
     */
    void update(Long id, MajorCreateDTO dto);

    /**
     * 删除by id相关逻辑。
     */
    void deleteById(Long id);
}
