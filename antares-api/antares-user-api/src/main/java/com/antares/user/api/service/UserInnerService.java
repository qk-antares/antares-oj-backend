package com.antares.user.api.service;

import com.antares.user.api.dto.SecretDTO;

public interface UserInnerService {
    SecretDTO getSecretByUid(Long uid);
}
