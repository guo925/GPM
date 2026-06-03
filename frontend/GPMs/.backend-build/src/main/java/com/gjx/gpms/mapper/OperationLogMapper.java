package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 通用操作日志 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
