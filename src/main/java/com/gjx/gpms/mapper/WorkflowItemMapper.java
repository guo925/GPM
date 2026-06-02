package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.WorkflowItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通用流程事项 Mapper
 */
@Mapper
public interface WorkflowItemMapper extends BaseMapper<WorkflowItem> {
}
