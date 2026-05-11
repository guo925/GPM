package com.baomidou.mapper;

import com.baomidou.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 教师基本信息（含管理员） Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

}
