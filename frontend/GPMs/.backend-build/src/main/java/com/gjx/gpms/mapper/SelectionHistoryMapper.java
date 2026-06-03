package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.SelectionHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选题记录历史表 Mapper。
 */
@Mapper
public interface SelectionHistoryMapper extends BaseMapper<SelectionHistory> {
}
