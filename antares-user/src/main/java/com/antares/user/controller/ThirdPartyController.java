package com.antares.user.controller;

import javax.annotation.Resource;
import javax.validation.constraints.Email;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.service.user.ThirdPartyService;
import com.antares.common.utils.R;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class ThirdPartyController {
    @Resource
    private ThirdPartyService thirdPartyService;

    /**
     * 发送邮箱验证码
     * 
     * @param email
     * @return
     */
    @GetMapping(value = "/email/sendCode")
    public R<Void> sendMailCode(@Email @RequestParam("email") String email) {
        thirdPartyService.sendMailCode(email);
        return R.ok();
    }
}
