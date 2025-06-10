package com.antares.user.controller;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/test")
// @Validated
public class TestController {
    @Data
    public static class UserDTO {
        @NotBlank(message = "用户名不能为空")
        private String name;

        @Min(value = 1, message = "用户ID必须大于0")
        private int id;
    }


    // 对单个参数进行校验
    @GetMapping("/user")
    public String getUser(@RequestParam @NotBlank String name, @Min(1) @RequestParam int id) {
        return name + id;
    }

    @PostMapping("/user")
    public String postUser(@RequestBody @Validated UserDTO userDTO) {
        return userDTO.getName() + userDTO.getId();
    }
}
