package com.antares.member.controller;

import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.member.model.vo.user.FollowVo;
import com.antares.member.service.FollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/member")
public class FollowController {
    @Resource
    private FollowService followService;

    /**
     * 关注或取消关注
     * @param uid
     * @param request
     * @return
     */
    @PostMapping("/follow/{uid}")
    public R follow(@PathVariable("uid") Long uid, HttpServletRequest request){
        followService.follow(uid, request);
        return R.ok();
    }

    @GetMapping("/follows/of/current")
    public R<List<FollowVo>> getFollowsOfCurrent(HttpServletRequest request){
        List<FollowVo> follows = followService.getFollowsOfCurrent(request);
        return R.ok(follows);
    }

    @GetMapping("/follows/of/{uid}")
    public R<List<UserInfoVo>> getFollowsByUid(@PathVariable("uid") Long uid, HttpServletRequest request){
        List<UserInfoVo> follows = followService.getFollowsByUid(uid, request);
        return R.ok(follows);
    }

    @GetMapping("/fans/of/{uid}")
    public R<List<UserInfoVo>> getFansByUid(@PathVariable("uid") Long uid, HttpServletRequest request){
        List<UserInfoVo> fans = followService.getFansByUid(uid, request);
        return R.ok(fans);
    }
}
