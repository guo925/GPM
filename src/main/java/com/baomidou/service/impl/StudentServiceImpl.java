package com.baomidou.service.impl;

import com.baomidou.entity.Student;
import com.baomidou.mapper.StudentMapper;
import com.baomidou.service.IStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 学生基本信息 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

}
