package com.antares.member.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class UserRegisterRequest {
    @Length(min = 6,max = 18,message = "密码必须是6—18位字符")
    private String password;

    @Email
    private String email;

    @NotEmpty(message = "验证码不能为空")
    private String captcha;
}