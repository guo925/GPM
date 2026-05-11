package com.baomidou.mapper;

import com.baomidou.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生基本信息 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
