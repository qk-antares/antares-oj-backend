package com.antares.oj.aop;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.annotation.AuthCheck;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.model.enums.UserRoleEnum;
import com.antares.oj.utils.UserUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Antares
 * @date 2023/8/24 17:49
 * @description 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserFeignService userFeignService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        // 当前登录用户
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(AppHttpCodeEnum.NO_AUTH);
            }
            String userRole = currentUser.getUserRole();
            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(AppHttpCodeEnum.NO_AUTH);
            }
            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(AppHttpCodeEnum.NO_AUTH);
                }
            }
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

