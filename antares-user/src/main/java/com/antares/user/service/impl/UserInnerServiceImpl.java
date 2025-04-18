package com.antares.user.service.impl;

import org.apache.dubbo.config.annotation.DubboService;

import com.antares.user.api.dto.SecretDTO;
import com.antares.user.api.service.UserInnerService;
import com.antares.user.mapper.UserMapper;
import com.antares.user.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@DubboService
public class UserInnerServiceImpl extends ServiceImpl<UserMapper, User> implements UserInnerService {
    @Override
    public SecretDTO getSecretByUid(Long uid) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().select(User::getSecretId, User::getSecretKey).eq(User::getUid, uid));
        SecretDTO secretDTO = new SecretDTO();
        secretDTO.setSecretId(user.getSecretId());
        secretDTO.setSecretKey(user.getSecretKey());
        return secretDTO;
    }
}
