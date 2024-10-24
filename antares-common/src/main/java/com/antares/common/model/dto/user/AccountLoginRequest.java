package com.antares.common.model.dto.user;

import javax.validation.constraints.Email;

import lombok.Data;

@Data
public class AccountLoginRequest {
    @Email
    private String email;
    private String password;
}