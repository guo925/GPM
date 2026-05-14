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

    void submit(ProcessSubmitDTO dto);

    void review(ProcessReviewDTO dto);

    List<ProcessInstanceVO> getByStudentTopic(Long studentTopicId);

    ProcessInstanceVO getCurrentStage(Long studentTopicId, String stage);
}
