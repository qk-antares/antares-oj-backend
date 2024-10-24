package com.antares.common.service.user;

import com.antares.common.model.entity.User;
import com.antares.common.model.vo.user.UserVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    UserVo getCurrentUser(String token);
}
