package com.antares.user.model.dto;

import javax.validation.constraints.Email;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class CodeLoginReq {
    @Email(message = "邮箱格式不正确")
    private String email;
    @Length(min = 6,max = 6,message = "验证码是长度为6的数字")
    private String captcha;
}
