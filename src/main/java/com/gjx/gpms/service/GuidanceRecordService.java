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

    /**
     * 创建相关逻辑。
     */
    void create(GuidanceRecordCreateDTO dto);

    /**
     * 审核相关逻辑。
     */
    void review(Long id, String comment);

    /**
     * 获取ByStudentTopic。
     */
    List<GuidanceRecordVO> getByStudentTopic(Long studentTopicId);
}
