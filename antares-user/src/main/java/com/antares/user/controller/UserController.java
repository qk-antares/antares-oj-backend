package com.antares.user.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.model.dto.R;
import com.antares.common.model.vo.user.UserVo;
import com.antares.common.service.user.UserService;
import com.antares.common.utils.TokenUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping
public class UserController {
    @Resource
    private UserService userService;

    @GetMapping(value = "/current")
    public R<UserVo> getCurrentUser() {
        String token = TokenUtils.getToken();
        UserVo user = userService.getCurrentUser(token);
        return R.ok(user);
    }
}
