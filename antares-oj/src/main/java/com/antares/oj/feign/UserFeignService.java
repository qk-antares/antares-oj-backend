package com.antares.oj.feign;

import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("antares-member")
public interface UserFeignService {
    /**
     * 获取当前登录用户
     * @return
     */
    @GetMapping("/member/info")
    R<UserInfoVo> getCurrentUser();

    /**
     * 根据uid获取用户信息
     * @param uid
     * @return
     */
    @GetMapping("/member/info/{uid}")
    R<UserInfoVo> info(@PathVariable("uid") Long uid);
}