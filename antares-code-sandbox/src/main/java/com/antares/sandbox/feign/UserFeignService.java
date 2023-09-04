package com.antares.sandbox.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("antares-member")
public interface UserFeignService {
    /**
     * 获取当前登录用户
     * @return
     */
    @PostMapping("/member/secretKey")
    String getSecretKey(String accessKey);
}