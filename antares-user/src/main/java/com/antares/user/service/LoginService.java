package com.antares.user.service;

import javax.servlet.http.HttpServletResponse;

import com.antares.user.model.dto.AccountLoginReq;
import com.antares.user.model.dto.CodeLoginReq;
import com.antares.user.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LoginService extends IService<User> {
    void sendMailCode(String email);

    void loginByAccount(AccountLoginReq req, HttpServletResponse res);

    void loginByCode(CodeLoginReq req, HttpServletResponse res);
}
