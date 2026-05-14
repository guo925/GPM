package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.GuidanceRecordCreateDTO;
import com.gjx.gpms.entity.GuidanceRecord;
import com.gjx.gpms.vo.GuidanceRecordVO;

import java.util.List;

/**
 * 指导记录服务接口
 *
 * @author gpms
 */
public interface GuidanceRecordService extends IService<GuidanceRecord> {

    void create(GuidanceRecordCreateDTO dto);

    void review(Long id, String comment);

    List<GuidanceRecordVO> getByStudentTopic(Long studentTopicId);
}
