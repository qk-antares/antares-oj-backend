package com.antares.member.controller;

import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.member.model.dto.user.PwdUpdateRequest;
import com.antares.member.model.dto.user.UserUpdateRequest;
import com.antares.member.model.vo.user.RecommendUserVo;
import com.antares.member.service.UserService;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 
 *
 * @author antares
 * @email 1716607668@qq.com
 * @date 2023-03-02 00:19:13
 */
@RestController
@RequestMapping("/member")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户的信息
     * @param request
     * @return
     */
    @GetMapping("/info")
    public R<UserInfoVo> getCurrentUser(HttpServletRequest request){
        UserInfoVo currentUser = userService.getCurrentUser(request);
        return R.ok(currentUser);
    }

    /**
     * 更新当前用户的基本信息
     * @param updateVo
     * @param request
     * @return
     */
    @PostMapping("/update")
    public R update(@RequestBody UserUpdateRequest updateVo, HttpServletRequest request){
        userService.updateCurrentUserInfo(updateVo, request);
        return R.ok();
    }

    /**
     * 更新用户的密码（因为这个牵涉旧密码的比较，和基本信息更新的逻辑差异很大，所以不能写在一起）
     * @param pwdUpdateRequest
     * @param request
     * @return
     */
    @PutMapping("/pwd")
    public R updatePwd(@RequestBody PwdUpdateRequest pwdUpdateRequest, HttpServletRequest request){
        userService.updatePwd(pwdUpdateRequest, request);
        return R.ok();
    }

    /**
     * 更新用户绑定的手机
     * @param phone
     * @param captcha
     * @param request
     * @return
     */
    @PutMapping("/phone")
    public R bindPhone(@Pattern(regexp = "^1([3-9])[0-9]{9}$", message = "手机号格式不正确") @RequestParam String phone,
                       @Length(min = 6, max = 6, message = "验证码格式不正确") @RequestParam String captcha,
                       HttpServletRequest request){
        userService.bindPhone(phone, captcha, request);
        return R.ok();
    }

    /**
     * 更新用户绑定的邮箱
     * @param email
     * @param captcha
     * @param request
     * @return
     */
    @PutMapping("/email")
    public R updateMail(@Email @RequestParam String email,
                       @Length(min = 6, max = 6, message = "验证码格式不正确") @RequestParam String captcha,
                        HttpServletRequest request){
        userService.updateMail(email, captcha, request);
        return R.ok();
    }

    /**
     * 根据id获取用户信息，包含当前用户是否关注了该用户的信息
     * @param uid
     * @param request
     * @return
     */
    @GetMapping("/info/{uid}")
    public R<UserInfoVo> info(@PathVariable("uid") Long uid, HttpServletRequest request){
        UserInfoVo userByUid = userService.getUserByUid(uid, request);
        return R.ok(userByUid);
    }

    /**
     * 获取标签相似的推荐用户
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public R<List<RecommendUserVo>> getRecommendUsers(HttpServletRequest request){
        List<RecommendUserVo> recommendUsers = userService.getRecommendUsers(request);
        return R.ok(recommendUsers);
    }

    /**
     * 刷新推荐用户
     * @param request
     * @return
     */
    @GetMapping("/recommend/refresh")
    public R<List<RecommendUserVo>> refreshRecommendUsers(HttpServletRequest request){
        List<RecommendUserVo> recommendUsers = userService.refreshRecommendUsers(request);
        return R.ok(recommendUsers);
    }
}
