package com.antares.user.service;

import javax.servlet.http.HttpServletResponse;

import com.antares.common.model.dto.user.AccountLoginRequest;
import com.antares.common.model.dto.user.CodeLoginReq;
import com.antares.common.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LoginService extends IService<User> {
    void loginByAccount(AccountLoginRequest req, HttpServletResponse res);

    void loginByCode(CodeLoginReq req, HttpServletResponse res);

    void logout(String token);
}
