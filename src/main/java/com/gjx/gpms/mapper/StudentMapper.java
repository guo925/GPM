package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生基本信息 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
