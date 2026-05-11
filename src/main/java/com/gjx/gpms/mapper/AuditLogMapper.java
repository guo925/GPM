package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 关键操作审计日志 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-11
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

}
