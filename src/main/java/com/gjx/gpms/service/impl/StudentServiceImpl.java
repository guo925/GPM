package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Student;
import com.gjx.gpms.mapper.StudentMapper;
import com.gjx.gpms.service.IStudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 学生基本信息 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

}
