package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.SelectionSubmitDTO;
import com.gjx.gpms.dto.TeacherReviewDTO;
import com.gjx.gpms.entity.SelectionRecord;
import com.gjx.gpms.vo.SelectionRecordVO;

import java.util.List;

/**
 * 选题记录服务接口
 *
 * @author gpms
 */
public interface SelectionRecordService extends IService<SelectionRecord> {

    /**
     * 提交preferences相关逻辑。
     */
    void submitPreferences(SelectionSubmitDTO dto);

    /**
     * 获取MySelections。
     */
    List<SelectionRecordVO> getMySelections(Long batchId, String grade);

    /**
     * 获取TeacherReviewList。
     */
    List<SelectionRecordVO> getTeacherReviewList(Long batchId, String grade);

    /**
     * 处理teacherReview相关逻辑。
     */
    void teacherReview(TeacherReviewDTO dto);

    /**
     * 处理autoAllocate相关逻辑。
     */
    void autoAllocate(Long batchId);
}
