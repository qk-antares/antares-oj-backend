package com.antares.user.service;

import org.apache.dubbo.config.annotation.DubboService;

import com.antares.common.mapper.UserMapper;
import com.antares.common.model.entity.User;
import com.antares.common.model.vo.user.UserVo;
import com.antares.common.service.user.UserService;
import com.antares.common.utils.TokenUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public UserVo getCurrentUser(String token) {
        Long uid = TokenUtils.getUidFromToken(token);
        User user = baseMapper.selectById(uid);
        return UserVo.userToVo(user);
    }
}
