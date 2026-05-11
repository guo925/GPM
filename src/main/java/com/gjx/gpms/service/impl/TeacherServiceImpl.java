package com.gjx.gpms.service.impl;

import com.gjx.gpms.entity.Teacher;
import com.gjx.gpms.mapper.TeacherMapper;
import com.gjx.gpms.service.ITeacherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 教师基本信息（含管理员） 服务实现类
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

}
