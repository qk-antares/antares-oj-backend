package com.antares.oj.utils;

import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;

public class UserUtils {
    public static UserInfoVo getCurrentUser(R<UserInfoVo> response){
        if (response.getCode() == AppHttpCodeEnum.SUCCESS.getCode()) {
            return response.getData();
        } else {
            throw new BusinessException(AppHttpCodeEnum.getEnumByCode(response.getCode()));
        }
    }

    public static boolean isAdmin(UserInfoVo user){
        return user.getUserRole().equals("admin");
    }
}
