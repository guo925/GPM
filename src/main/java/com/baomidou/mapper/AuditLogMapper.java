package com.baomidou.mapper;

import com.baomidou.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 关键操作审计日志 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

}
