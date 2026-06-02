package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.DefenseBatchDTO;
import com.gjx.gpms.dto.DefenseGroupDTO;
import com.gjx.gpms.dto.DefenseResultDTO;
import com.gjx.gpms.entity.*;

import java.util.List;

/**
 * 答辩服务接口
 */
public interface DefenseService {

    // 答辩批次
    /**
     * 查询列表batches相关逻辑。
     */
    List<DefenseBatch> listBatches(Long batchId);
    /**
     * 创建batch相关逻辑。
     */
    void createBatch(DefenseBatchDTO dto);
    /**
     * 删除batch相关逻辑。
     */
    void deleteBatch(Long id);

    // 答辩组
    /**
     * 查询列表groups相关逻辑。
     */
    List<DefenseGroup> listGroups(Long defenseBatchId);
    /**
     * 创建group相关逻辑。
     */
    void createGroup(DefenseGroupDTO dto);
    /**
     * 删除group相关逻辑。
     */
    void deleteGroup(Long id);

    // 答辩安排
    /**
     * 查询列表arrangements相关逻辑。
     */
    List<DefenseArrangement> listArrangements(Long groupId);
    /**
     * 新增arrangement相关逻辑。
     */
    void addArrangement(Long groupId, Long studentId, String defenseTime, String location);

    // 答辩结果
    /**
     * 保存result相关逻辑。
     */
    void saveResult(DefenseResultDTO dto);
    /**
     * 获取Result。
     */
    DefenseResult getResult(Long arrangementId);
}
