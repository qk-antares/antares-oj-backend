package com.antares.common.service.user;

import com.antares.common.model.dto.user.AccountLoginRequest;
import com.antares.common.model.dto.user.CodeLoginRequest;
import com.antares.common.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LoginService extends IService<User> {
    String loginByAccount(AccountLoginRequest accountLoginRequest);

    String loginByCode(CodeLoginRequest codeLoginRequest);

    void logout(String token);
}
