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
    List<DefenseBatch> listBatches(Long batchId);
    void createBatch(DefenseBatchDTO dto);
    void deleteBatch(Long id);

    // 答辩组
    List<DefenseGroup> listGroups(Long defenseBatchId);
    void createGroup(DefenseGroupDTO dto);
    void deleteGroup(Long id);

    // 答辩安排
    List<DefenseArrangement> listArrangements(Long groupId);
    void addArrangement(Long groupId, Long studentId, String defenseTime, String location);

    // 答辩结果
    void saveResult(DefenseResultDTO dto);
    DefenseResult getResult(Long arrangementId);
}
