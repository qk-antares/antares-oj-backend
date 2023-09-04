package com.antares.oj.constant;

/**
 * @author Antares
 * @date 2023/8/24 17:51
 * @description 用户常量
 */
public interface UserConstant {
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion
}
