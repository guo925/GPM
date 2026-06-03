package com.gjx.gpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.BatchCreateDTO;
import com.gjx.gpms.dto.BatchUpdateDTO;
import com.gjx.gpms.entity.Batch;
import com.gjx.gpms.vo.BatchVO;

import java.util.List;

/**
 * 批次服务接口
 *
 * @author gpms
 */
public interface BatchService extends IService<Batch> {

    /**
     * 分页查询相关逻辑。
     */
    IPage<BatchVO> page(long current, long size, String name, Integer status);

    /**
     * 获取Detail。
     */
    BatchVO getDetail(Long id);

    /**
     * 获取CurrentBatch。
     */
    BatchVO getCurrentBatch();

    /**
     * 创建相关逻辑。
     */
    void create(BatchCreateDTO dto);

    /**
     * 更新相关逻辑。
     */
    void update(BatchUpdateDTO dto);

    /**
     * 删除by id相关逻辑。
     */
    void deleteById(Long id);

    /**
     * 处理advanceStage相关逻辑。
     */
    void advanceStage(Long id, String nextStage);

    /**
     * 根据年级解析所有批次ID列表。
     */
    List<Long> resolveBatchIdsByGrade(String grade);

    /**
     * 获取所有不重复的年级列表。
     */
    List<String> getDistinctGrades();
}
