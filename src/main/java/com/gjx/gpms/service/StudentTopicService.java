package com.gjx.gpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjx.gpms.entity.StudentTopic;
import com.gjx.gpms.vo.StudentTopicVO;

/**
 * 学生选题结果服务接口
 *
 * @author gpms
 */
public interface StudentTopicService extends IService<StudentTopic> {

    IPage<StudentTopicVO> page(long current, long size, Long batchId, Long advisorId);

    StudentTopicVO getByStudentId(Long studentId);
}
