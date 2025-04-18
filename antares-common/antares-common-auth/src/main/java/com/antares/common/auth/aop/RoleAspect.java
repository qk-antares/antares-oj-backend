package com.antares.common.auth.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.antares.common.auth.annotation.RoleCheck;
import com.antares.common.auth.enums.UserRoleEnum;
import com.antares.common.auth.utils.TokenUtils;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;

/**
 * @author Antares
 * @date 2023/8/24 17:49
 * @description 权限校验 AOP
 */
@Aspect
@Component
@Order(2)
public class RoleAspect {
    /**
     * 执行拦截
     * 
     * @param joinPoint
     * @param roleCheck
     * @return
     */
    @Before("@annotation(com.antares.common.auth.annotation.RoleCheck)")
    public void doBefore(RoleCheck roleCheck) throws Throwable {
        // 从Cookie中获取Token
        String token = TokenUtils.getToken();

        JWT jwt = JWT.of(token);
        String userRole = (String) jwt.getPayload("userRole");
        
        String mustRole = roleCheck.mustRole();
        // 必须有该权限才通过
        if (StrUtil.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(HttpCodeEnum.NO_AUTH);
            }
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(HttpCodeEnum.NO_AUTH);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(HttpCodeEnum.NO_AUTH);
                }
            }
        }
    }
}
