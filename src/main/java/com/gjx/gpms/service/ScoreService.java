package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.ScoreSheetDTO;
import com.gjx.gpms.entity.ScoreDetail;
import com.gjx.gpms.entity.ScoreSheet;
import com.gjx.gpms.vo.ScoreSheetVO;

import java.util.List;

/**
 * 成绩服务接口
 */
public interface ScoreService extends IService<ScoreSheet> {

    /**
     * 计算相关逻辑。
     */
    ScoreSheetVO calculate(ScoreSheetDTO dto);

    /**
     * 提交相关逻辑。
     */
    void submit(Long id);

    /**
     * 审核相关逻辑。
     */
    void review(Long id, String status, String comment);

    /**
     * 获取Detail。
     */
    ScoreSheetVO getDetail(Long studentTopicId);

    /**
     * 查询列表by batch相关逻辑。
     */
    List<ScoreSheetVO> listByBatch(Long batchId, String grade);
}
