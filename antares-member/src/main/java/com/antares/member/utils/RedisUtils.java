package com.antares.member.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import com.antares.common.constant.RedisConstants;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.utils.HttpUtils;
import com.antares.common.utils.ObjectMapperUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.antares.common.utils.ObjectMapperUtils.MAPPER;

@Component
public class RedisUtils {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public UserInfoVo getCurrentUserWithValidation(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }
        String token = HttpUtils.getToken(request);
        // 如果找到了token，则从Redis中获取对应的用户信息
        if (token != null) {
            try {
                String userInfoJSON = stringRedisTemplate.opsForValue().get(RedisConstants.USER_SESSION_PREFIX + token);
                //redis中不存在这个token
                if(StringUtils.isEmpty(userInfoJSON)){
                    throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
                } else {
                    UserInfoVo userInfoVo = MAPPER.readValue(userInfoJSON, UserInfoVo.class);
                    return userInfoVo;
                }
            } catch (JsonProcessingException e) {
                throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "JSON转换异常");
            }
        } else {
            // 没有token
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }
    }

    public UserInfoVo getCurrentUser(HttpServletRequest request){
        if(request == null){
            return null;
        }
        String token = HttpUtils.getToken(request);
        // 如果找到了token，则从Redis中获取对应的用户信息
        if (token != null) {
            try {
                String userInfoJSON = stringRedisTemplate.opsForValue().get(RedisConstants.USER_SESSION_PREFIX + token);
                //redis中不存在这个token
                if(StringUtils.isEmpty(userInfoJSON)){
                    return null;
                } else {
                    UserInfoVo userInfoVo = MAPPER.readValue(userInfoJSON, UserInfoVo.class);
                    return userInfoVo;
                }
            } catch (JsonProcessingException e) {
                throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "JSON转换异常");
            }
        } else {
            // 没有token
            return null;
        }
    }

    public <T> void rightPushAllAsString(String cacheKey, List<T> list){
        stringRedisTemplate.opsForList().rightPushAll(cacheKey,
                list.stream().map(item -> ObjectMapperUtils.writeValueAsString(item)).collect(Collectors.toList()));
    }

    public <T> List<T> readList(String cacheKey, Class<T> clazz) {
        return stringRedisTemplate.opsForList().range(cacheKey, 0, -1)
                .stream().map(itemStr -> {
                    try {
                        return MAPPER.readValue(itemStr, clazz);
                    } catch (JsonProcessingException e) {
                        throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR);
                    }
                }).collect(Collectors.toList());
    }
}
