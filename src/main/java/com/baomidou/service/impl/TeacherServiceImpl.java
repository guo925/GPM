package com.baomidou.service.impl;

import com.baomidou.entity.Teacher;
import com.baomidou.mapper.TeacherMapper;
import com.baomidou.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 教师基本信息（含管理员） 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

}
