package com.baomidou.mapper;

import com.baomidou.entity.Batch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 毕设批次及各阶段时间开关 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2026-05-11
 */
@Mapper
public interface BatchMapper extends BaseMapper<Batch> {

}
