package com.antares.user.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Email;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.core.dto.R;
import com.antares.user.model.dto.AccountLoginReq;
import com.antares.user.model.dto.CodeLoginReq;
import com.antares.user.service.LoginService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class LoginController {
    @Resource
    private LoginService loginService;

    /**
     * 发送邮箱验证码
     * 
     * @param email
     * @return
     */
    @GetMapping(value = "/email/sendCode")
    public R<Void> sendMailCode(@Email @RequestParam("email") String email) {
        loginService.sendMailCode(email);
        return R.ok();
    }

    /**
     * 账号密码登录
     * @param accountLoginReq
     * @param response
     * @return
     */
    @PostMapping(value = "/login")
    public R<Void> loginByAccount(@Valid @RequestBody AccountLoginReq accountLoginReq, HttpServletResponse response) {
        loginService.loginByAccount(accountLoginReq, response);
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
}
