package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.ProcessInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 毕设流程实例表(状态机) Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Mapper
public interface ProcessInstanceMapper extends BaseMapper<ProcessInstance> {

}
