package com.antares.member.model.dto.user;

import lombok.Data;

@Data
public class AccountLoginRequest {
    private String account;
    private String password;
}