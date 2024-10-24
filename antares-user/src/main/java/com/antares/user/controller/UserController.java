package com.antares.user.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.TokenCheck;
import com.antares.common.model.vo.user.UserVo;
import com.antares.common.service.user.UserService;
import com.antares.common.utils.R;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping(value = "/current")
    @TokenCheck
    public R<UserVo> getCurrentUser(@RequestHeader("Authorization") String token) {
        UserVo user = userService.getCurrentUser(token);
        return R.ok(user);
    }
}
