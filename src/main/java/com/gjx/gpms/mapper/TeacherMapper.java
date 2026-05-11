package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 教师基本信息（含管理员） Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

}
