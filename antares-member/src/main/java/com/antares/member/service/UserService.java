package com.antares.member.service;

import com.antares.member.model.dto.user.*;
import com.antares.member.model.entity.User;
import com.antares.member.model.vo.user.RecommendUserVo;
import com.antares.member.model.vo.user.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.dto.UsernameAndAvtarDto;
import com.antares.common.model.vo.UserInfoVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author antares
 * @email 1716607668@qq.com
 * @date 2023-03-02 00:19:13
 */
public interface UserService extends IService<User> {
    void sendCode(String dest, int type);

    void register(UserRegisterRequest userRegisterRequest);

    void checkPhoneUnique(String phone) throws BusinessException;

    void checkUsernameUnique(String username) throws BusinessException;

    void checkEmailUnique(String email) throws BusinessException;

    void login(AccountLoginRequest accountLoginRequest, HttpServletResponse response);

    void oauthLogin(SocialUser socialUser, HttpServletResponse response) throws IOException;

    UserInfoVo getCurrentUser(HttpServletRequest request);

    void updateCurrentUserInfo(UserUpdateRequest updateVo, HttpServletRequest request);

    void updatePwd(PwdUpdateRequest pwdUpdateRequest, HttpServletRequest request);

    void bindPhone(String phone, String code, HttpServletRequest request);

    void updateMail(String mail, String code, HttpServletRequest request);

    void loginByPhone(PhoneLoginRequest vo, HttpServletResponse response);

    UserInfoVo getUserByUid(Long uid, HttpServletRequest request);

    List<UserInfoVo> getUserListByUids(List<Long> uids, HttpServletRequest request);

    List<RecommendUserVo> getRecommendUsers(HttpServletRequest request);

    /**
     * 获取推荐用户，并将推荐用户uid缓存到redis
     * @param uid 当前uid
     * @param tags 当前tags
     * @param tagCount userTag总数
     * @param loopCount 循环次数，超出这个次数还没找到8个相似度超过阈值的，余下的随机填充
     * @return uid和score组成的hashmap
     */
    Map<String, Double> getRecommendUserIdsAndCache(Long uid, String tags, int tagCount, int loopCount);

    /**
     * 获取随机推荐用户，并将推荐用户uid缓存到redis（为未登录用户定制）
     * @param tagCount userTag总数
     * @param loopCount 循环次数，超出这个次数还没找到8个相似度超过阈值的，余下的随机填充
     * @return uid和score组成的hashmap
     */
    Map<String, Double> getRandomUserIdsAndCache(int tagCount, int loopCount);

    List<RecommendUserVo> refreshRecommendUsers(HttpServletRequest request);

    List<UsernameAndAvtarDto> listUserNameAndAvatarByUids(Collection<Long> uids);

    UsernameAndAvtarDto getUsernameAndAvatar(Long uid);
}

