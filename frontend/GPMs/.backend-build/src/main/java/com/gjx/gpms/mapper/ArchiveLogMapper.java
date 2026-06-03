package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.ArchiveLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 冷热数据归档日志 Mapper。
 */
@Mapper
public interface ArchiveLogMapper extends BaseMapper<ArchiveLog> {
}
