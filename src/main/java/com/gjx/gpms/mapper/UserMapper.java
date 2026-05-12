package com.gjx.gpms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author gpms
 * @since 2026-05-12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
