package com.antares.user.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.TokenCheck;
import com.antares.common.model.dto.R;
import com.antares.common.model.dto.user.AccountLoginRequest;
import com.antares.common.model.dto.user.CodeLoginReq;
import com.antares.common.utils.TokenUtils;
import com.antares.user.service.LoginService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 账号密码登录
     * @param accountLoginRequest
     * @param response
     * @return
     */
    @PostMapping(value = "/login")
    public R<Void> loginByAccount(@Valid @RequestBody AccountLoginRequest accountLoginRequest, HttpServletResponse response) {
        loginService.loginByAccount(accountLoginRequest, response);
        return R.ok();
    }

    /**
     * 验证码登录
     * @param phoneLoginRequest
     * @param res
     * @return
     */
    @PostMapping(value = "/loginByCode")
    public R<Void> loginByCode(@Valid @RequestBody CodeLoginReq req, HttpServletResponse res) {
        loginService.loginByCode(req, res);
        return R.ok();
    }

    @PostMapping(value = "/logout")
    @TokenCheck
    public R<Void> logout() {
        loginService.logout(TokenUtils.getToken());
        return R.ok();
    }
}
