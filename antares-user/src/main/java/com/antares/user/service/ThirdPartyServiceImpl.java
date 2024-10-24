package com.antares.user.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.antares.common.constant.RedisConstants;
import com.antares.common.exception.BusinessException;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.entity.User;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.common.service.user.ThirdPartyService;
import com.antares.user.utils.MailUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ThirdPartyServiceImpl extends ServiceImpl<UserMapper, User> implements ThirdPartyService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MailUtil mailUtil;

    @Override
    public void sendMailCode(String email) {
        String redisCodeKey = RedisConstants.MAIL_CODE_CACHE_PREFIX + email;
        String redisCode = stringRedisTemplate.opsForValue().get(redisCodeKey);
        // 1、接口防刷
        // 发送过验证码了
        if (!StrUtil.isEmpty(redisCode)) {
            // 用当前时间减去存入redis的时间，判断用户邮箱是否在60s内发送过验证码
            long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                // 60s内不能再发
                throw new BusinessException(HttpCodeEnum.CODE_EXCEPTION);
            }
        }

        // 2、存入redis，value是"codeNum_系统时间"
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        String redisStorage = codeNum + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(redisCodeKey, redisStorage, 10, TimeUnit.MINUTES);

        // 3、异步发送验证码
        mailUtil.sendMail(email, codeNum);

        log.info("向email{}发送验证码: {}", email, code);
    }

}
