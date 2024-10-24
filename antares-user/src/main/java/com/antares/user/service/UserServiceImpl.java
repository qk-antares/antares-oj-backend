package com.antares.user.service;

import org.apache.dubbo.config.annotation.DubboService;

import com.antares.common.mapper.UserMapper;
import com.antares.common.model.entity.User;
import com.antares.common.model.vo.user.UserVo;
import com.antares.common.service.user.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.convert.NumberWithFormat;
import cn.hutool.jwt.JWT;

@DubboService
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public UserVo getCurrentUser(String token) {
        JWT jwt = JWT.of(token.substring(7));
        Long uid = (Long) ((NumberWithFormat) jwt.getPayload("uid")).getNumber();
        User user = baseMapper.selectById(uid);
        return UserVo.userToVo(user);
    }
}
