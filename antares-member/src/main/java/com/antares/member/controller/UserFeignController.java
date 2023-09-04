package com.antares.member.controller;

import com.antares.common.model.dto.UsernameAndAvtarDto;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.member.model.entity.User;
import com.antares.member.service.ConversationService;
import com.antares.member.service.FollowService;
import com.antares.member.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/member")
public class UserFeignController {
    @Resource
    private UserService userService;
    @Resource
    private FollowService followService;
    @Resource
    private ConversationService conversationService;

    @PostMapping("/list/username/avatar")
    public List<UsernameAndAvtarDto> getUsernameAndAvatarByUids(@RequestBody Collection<Long> uids){
        return userService.listUserNameAndAvatarByUids(uids);
    }

    @GetMapping("/{uid}/username/avatar")
    public UsernameAndAvtarDto getUsernameAndAvatar(@PathVariable("uid") Long uid){
        return userService.getUsernameAndAvatar(uid);
    }

    @GetMapping("/followIds/of/current")
    public List<Long> getFollowIdsOfCurrent(HttpServletRequest request){
        return followService.getFollowIdsOfCurrent(request);
    }

    /**
     * 根据uid列表获取用户信息，
     * 根据uid列表获取用户信息，
     * @param uids
     * @param request
     * @return
     */
    @PostMapping("/info/list")
    public List<UserInfoVo> getUserListByUids(@RequestBody List<Long> uids, HttpServletRequest request){
        return userService.getUserListByUids(uids, request);
    }

    @PostMapping("/conversation/clear")
    public void clearConversationUnread(@RequestParam("uid") Long uid){
        conversationService.clearConversationUnread(uid);
    }

    @PostMapping("/secretKey")
    public String getSecretKey(@RequestBody String accessKey){
        return userService.getOne(new LambdaQueryWrapper<User>()
                .select(User::getSecretKey).eq(User::getAccessKey, accessKey))
                .getSecretKey();

    }
}
