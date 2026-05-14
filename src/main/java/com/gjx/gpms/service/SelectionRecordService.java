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

    void submitPreferences(SelectionSubmitDTO dto);

    List<SelectionRecordVO> getMySelections(Long batchId);

    List<SelectionRecordVO> getTeacherReviewList(Long batchId);

    void teacherReview(TeacherReviewDTO dto);

    void autoAllocate(Long batchId);
}
