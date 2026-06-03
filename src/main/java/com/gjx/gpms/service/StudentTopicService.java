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

    /**
     * 分页查询相关逻辑。
     */
    IPage<StudentTopicVO> page(long current, long size, Long batchId, String grade, Long advisorId);

    /**
     * 获取ByStudentId。
     */
    StudentTopicVO getByStudentId(Long studentId);
}
