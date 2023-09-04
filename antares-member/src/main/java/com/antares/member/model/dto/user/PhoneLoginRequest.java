package com.antares.member.model.dto.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

@Data
public class PhoneLoginRequest {
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;
    @Length(min = 6,max = 6,message = "验证码是长度为6的数字")
    private String captcha;
}
