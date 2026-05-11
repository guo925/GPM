package com.baomidou.mapper;

import com.baomidou.entity.Submission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生文档提交记录 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Mapper
public interface SubmissionMapper extends BaseMapper<Submission> {

}
