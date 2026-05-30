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

    ScoreSheetVO calculate(ScoreSheetDTO dto);

    void submit(Long id);

    void review(Long id, String status, String comment);

    ScoreSheetVO getDetail(Long studentTopicId);

    List<ScoreSheetVO> listByBatch(Long batchId);
}
