package com.gjx.gpms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjx.gpms.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 *
 * @author gpms
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 释放已逻辑删除用户占用的用户名唯一键。
     *
     * @param username 用户名
     * @return 更新行数
     */
    int releaseDeletedUsername(@Param("username") String username);

    /**
     * 删除前归档用户名，避免逻辑删除记录继续占用唯一键。
     *
     * @param id 用户ID
     * @return 更新行数
     */
    int archiveUsernameBeforeDelete(@Param("id") Long id);

}
