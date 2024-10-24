package com.antares.user.controller;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.TokenCheck;
import com.antares.common.model.dto.user.AccountLoginRequest;
import com.antares.common.model.dto.user.CodeLoginRequest;
import com.antares.common.service.user.LoginService;
import com.antares.common.utils.R;

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
    public R<String> loginByAccount(@Valid @RequestBody AccountLoginRequest accountLoginRequest) {
        String token = loginService.loginByAccount(accountLoginRequest);
        return R.ok(token);
    }

    /**
     * 验证码登录
     * @param phoneLoginRequest
     * @param response
     * @return
     */
    @PostMapping(value = "/loginByCode")
    public R<String> loginByCode(@Valid @RequestBody CodeLoginRequest codeLoginRequest) {
        String token = loginService.loginByCode(codeLoginRequest);
        return R.ok(token);
    }

    @PostMapping(value = "/logout")
    @TokenCheck
    public R<Void> logout(@RequestHeader("Authorization") String token) {
        loginService.logout(token);
        return R.ok();
    }
}
