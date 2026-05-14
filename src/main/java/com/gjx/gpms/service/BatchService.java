package com.gjx.gpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.BatchCreateDTO;
import com.gjx.gpms.dto.BatchUpdateDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.vo.BatchVO;

/**
 * 批次服务接口
 *
 * @author gpms
 */
public interface BatchService extends IService<Batch> {

    IPage<BatchVO> page(long current, long size, String name, Integer status);

    BatchVO getDetail(Long id);

    void create(BatchCreateDTO dto);

    void update(BatchUpdateDTO dto);

    void deleteById(Long id);

    void advanceStage(Long id, String nextStage);
}
