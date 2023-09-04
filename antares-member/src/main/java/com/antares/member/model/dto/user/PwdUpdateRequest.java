package com.antares.member.model.dto.user;

import lombok.Data;

@Data
public class PwdUpdateRequest {
    private String originalPwd;
    private String newPwd;
}
