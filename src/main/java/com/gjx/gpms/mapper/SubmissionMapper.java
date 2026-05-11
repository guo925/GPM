package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.Submission;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生文档提交记录 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

}
