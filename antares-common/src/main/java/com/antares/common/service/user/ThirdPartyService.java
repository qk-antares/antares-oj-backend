package com.antares.common.service.user;

import com.antares.common.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ThirdPartyService extends IService<User> {
    void sendMailCode(String email);
}
