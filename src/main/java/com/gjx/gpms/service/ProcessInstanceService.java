package com.gjx.gpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.dto.ProcessReviewDTO;
import com.gjx.gpms.dto.ProcessSubmitDTO;
import com.gjx.gpms.entity.ProcessInstance;
import com.gjx.gpms.vo.ProcessInstanceVO;

import java.util.List;

/**
 * 流程实例服务接口
 *
 * @author gpms
 */
public interface ProcessInstanceService extends IService<ProcessInstance> {

    /**
     * 提交相关逻辑。
     */
    void submit(ProcessSubmitDTO dto);

    /**
     * 审核相关逻辑。
     */
    void review(ProcessReviewDTO dto);

    /**
     * 获取ByStudentTopic。
     */
    List<ProcessInstanceVO> getByStudentTopic(Long studentTopicId);

    /**
     * 获取CurrentStage。
     */
    ProcessInstanceVO getCurrentStage(Long studentTopicId, String stage);
}
