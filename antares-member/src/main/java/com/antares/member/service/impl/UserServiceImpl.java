package com.antares.member.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.antares.common.constant.RedisConstants;
import com.antares.common.constant.SystemConstants;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.dto.UsernameAndAvtarDto;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.model.vo.UserTagVo;
import com.antares.common.utils.BeanCopyUtils;
import com.antares.common.utils.HttpUtils;
import com.antares.member.mapper.FollowMapper;
import com.antares.member.mapper.UserMapper;
import com.antares.member.mapper.UserTagMapper;
import com.antares.member.model.dto.user.*;
import com.antares.member.model.entity.Follow;
import com.antares.member.model.entity.User;
import com.antares.member.model.vo.user.GiteeUser;
import com.antares.member.model.vo.user.RecommendUserVo;
import com.antares.member.model.vo.user.SocialUser;
import com.antares.member.service.UserService;
import com.antares.member.service.UserTagService;
import com.antares.member.utils.AlgorithmUtils;
import com.antares.member.utils.RedisUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.antares.common.constant.RedisConstants.*;
import static com.antares.common.constant.SystemConstants.*;
import static com.antares.common.utils.ObjectMapperUtils.MAPPER;

/**
 * @author Antares
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserTagService userTagService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserTagMapper userTagMapper;
    @Resource
    private FollowMapper followMapper;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${antares.domain}")
    private String domain;

    @Override
    public void sendCode(String dest, int type) {
        String redisCodeKey = (type == PHONE_CODE ? RedisConstants.CODE_SMS_CACHE_PREFIX : RedisConstants.MAIL_CODE_CACHE_PREFIX) + dest;
        String redisCode = stringRedisTemplate.opsForValue().get(redisCodeKey);
        //1、接口防刷
        //发送过验证码了
        if (!StringUtils.isEmpty(redisCode)) {
            //用当前时间减去存入redis的时间，判断用户手机号是否在60s内发送验证码
            long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                //60s内不能再发
                throw new BusinessException(AppHttpCodeEnum.CODE_EXCEPTION);
            }
        }

        //2、key的形式是prefix:phone，value是codeNum_系统时间
        int code = (int) ((Math.random() * 9 + 1) * 100000);
        log.info("{}", code);
        String codeNum = String.valueOf(code);
        String redisStorage = codeNum + "_" + System.currentTimeMillis();

        //存入redis，防止同一个手机号在60秒内再次发送验证码
        stringRedisTemplate.opsForValue().set(redisCodeKey, redisStorage,10, TimeUnit.MINUTES);

        if(type == PHONE_CODE){
            rabbitTemplate.convertAndSend("exchange.direct", "code",
                    new String[]{"phone", dest, codeNum},
                    new CorrelationData(UUID.randomUUID().toString()));
        } else if (type == MAIL_CODE) {
            rabbitTemplate.convertAndSend("exchange.direct", "code",
                    new String[]{"mail", dest, codeNum},
                    new CorrelationData(UUID.randomUUID().toString()));
        }
    }

    @Override
    public void register(UserRegisterRequest userRegisterRequest) {
        //1、效验验证码
        String code = userRegisterRequest.getCaptcha();

        //获取存入Redis里的验证码
        String redisCodeKey = RedisConstants.MAIL_CODE_CACHE_PREFIX + userRegisterRequest.getEmail();
        String redisCode = stringRedisTemplate.opsForValue().get(redisCodeKey);
        //获取redis中验证码并进行截取
        if (!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])) {
            //删除验证码;令牌机制
            stringRedisTemplate.delete(redisCodeKey);
            //验证码通过，真正注册
            User user = new User();
            //检查邮箱是否唯一。感知异常，异常机制
            checkEmailUnique(userRegisterRequest.getEmail());

            user.setEmail(userRegisterRequest.getEmail());
            //密码进行MD5加密
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String encode = bCryptPasswordEncoder.encode(userRegisterRequest.getPassword());
            user.setPassword(encode);

            //保存数据
            baseMapper.insert(user);

            //设置用户名和AK/SK
            User update = new User();
            update.setUid(user.getUid());
            update.setUsername(USERNAME_PREFIX + user.getUid());
            update.setAccessKey(DigestUtil.sha1Hex(user.getUid() + RandomUtil.randomString(32)));
            update.setSecretKey(DigestUtil.sha1Hex(user.getUid() + RandomUtil.randomString(32)));
            baseMapper.updateById(update);
        }
        throw new BusinessException(AppHttpCodeEnum.WRONG_CODE);
    }

    @Override
    public void login(AccountLoginRequest accountLoginRequest, HttpServletResponse response) {
        String account = accountLoginRequest.getAccount();
        String password = accountLoginRequest.getPassword();

        //1、去数据库查询 SELECT * FROM antares_user WHERE email = ? OR phone = ?
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, account).or().eq(User::getPhone, account));

        if (user == null) {
            //登录失败
            throw new BusinessException(AppHttpCodeEnum.ACCOUNT_NOT_EXIST);
        } else {
            //获取到数据库里的password
            String passwordCrypt = user.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //进行密码匹配
            boolean matches = passwordEncoder.matches(password, passwordCrypt);
            if (matches) {
                //登录成功
                UserInfoVo userInfoVo = userToVo(user, null);
                generateTokenAndCookie(userInfoVo, response);
            } else {
                throw new BusinessException(AppHttpCodeEnum.WRONG_PASSWORD);
            }
        }
    }

    @Override
    public void oauthLogin(SocialUser socialUser, HttpServletResponse response) throws IOException {
        HashMap<String, String> param = new HashMap<>();
        param.put("access_token", socialUser.getAccess_token());
        HttpResponse giteeResponse;
        try {
            giteeResponse = HttpUtils.doGet("https://gitee.com", "/api/v5/user", new HashMap<>(), param);
        } catch (Exception e) {
            throw new BusinessException(AppHttpCodeEnum.THIRD_PARTY_EXCEPTION);
        }

        //查询这个用户的gitee信息
        if(giteeResponse.getStatusLine().getStatusCode() == 200){
            String userJson = EntityUtils.toString(giteeResponse.getEntity());
            GiteeUser giteeUser = JSON.parseObject(userJson, GiteeUser.class);

            //具有登录和注册逻辑
            String socialId = giteeUser.getId();
            //1、判断当前社交用户是否已经登录过系统
            User user = lambdaQuery().eq(User::getSocialUid, socialId).one();
            //这个用户已经注册过
            if (user != null) {
                //Todo: 更新令牌应该用异步优化
                //更新用户的访问令牌的时间和access_token
                User update = new User();
                update.setUid(user.getUid());
                update.setAccessToken(socialUser.getAccess_token());
                update.setExpiresIn(socialUser.getExpires_in());
                baseMapper.updateById(update);

                //设置新的访问令牌的时间和access_token
                user.setAccessToken(socialUser.getAccess_token());
                user.setExpiresIn(socialUser.getExpires_in());

                generateTokenAndCookie(userToVo(user, null), response);
            } else {
                //2、没有查到当前社交用户对应的记录我们就需要注册一个
                User register = new User();

                register.setUsername(USERNAME_PREFIX + UUID.randomUUID());
                register.setAvatar(giteeUser.getAvatar_url());
                register.setSocialUid(giteeUser.getId());
                register.setAccessToken(socialUser.getAccess_token());
                register.setExpiresIn(socialUser.getExpires_in());

                //把用户信息插入到数据库中
                baseMapper.insert(register);

                generateTokenAndCookie(userToVo(register, null), response);
            }
        }
    }

    @Override
    public UserInfoVo getCurrentUser(HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);
        //查询该用户的关注、粉丝
        User user = lambdaQuery().select(User::getFollow, User::getFans, User::getTopic)
                .eq(User::getUid, currentUser.getUid()).one();
        BeanUtils.copyProperties(user, currentUser, BeanCopyUtils.getNullPropertyNames(user));
        return currentUser;
    }

    @Override
    public void updateCurrentUserInfo(UserUpdateRequest updateVo, HttpServletRequest request) {
        //如果是当前用户才可以更新
        UserInfoVo userInfoVo = redisUtils.getCurrentUserWithValidation(request);
        if(userInfoVo.getUid().equals(updateVo.getUid())) {
            //用户名字段不允许重复，所以先查用户名
            String currentUserName = lambdaQuery().select(User::getUsername).eq(User::getUid, updateVo.getUid()).one().getUsername();
            if(!currentUserName.equals(updateVo.getUsername())){
                //有没有同名用户
                User one = lambdaQuery().eq(User::getUsername, updateVo.getUsername()).one();
                if(one != null){
                    throw new BusinessException(AppHttpCodeEnum.USERNAME_EXIST);
                }
            }
            //更新数据库
            User user = BeanCopyUtils.copyBean(updateVo, User.class);
            user.setTags(JSON.toJSONString(updateVo.getTags()));
            updateById(user);
            //更新redis
            BeanUtils.copyProperties(updateVo, userInfoVo, BeanCopyUtils.getNullPropertyNames(updateVo));
            userInfoVo.setTags(userTagService.idsToTags(updateVo.getTags()));

            flashRedis(userInfoVo, request);
        }else {
            throw new BusinessException(AppHttpCodeEnum.NO_AUTH);
        }
    }

    @Override
    public void updatePwd(PwdUpdateRequest pwdUpdateRequest, HttpServletRequest request) {
        //如果是当前用户才可以更新
        UserInfoVo userInfoVo = redisUtils.getCurrentUserWithValidation(request);
        //获取到数据库里的password
        String passwordCrypt = lambdaQuery().select(User::getPassword).eq(User::getUid, userInfoVo.getUid()).one().getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //进行密码匹配
        boolean matches = passwordEncoder.matches(pwdUpdateRequest.getOriginalPwd(), passwordCrypt);
        //原始密码输入正确
        if (matches) {
            User user = new User();
            user.setUid(userInfoVo.getUid());
            user.setPassword(passwordEncoder.encode(pwdUpdateRequest.getNewPwd()));
            updateById(user);
        } else {
            throw new BusinessException(AppHttpCodeEnum.WRONG_PASSWORD);
        }
    }

    @Override
    public void bindPhone(String phone, String code, HttpServletRequest request) {
        UserInfoVo userInfoVo = redisUtils.getCurrentUserWithValidation(request);
        //从redis中读取验证码
        String cacheKey = RedisConstants.CODE_SMS_CACHE_PREFIX + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(cacheKey);
        //验证码校验通过
        if(!StringUtils.isEmpty(cacheCode) && code.equals(cacheCode.split("_")[0])){
            //删除redis中的验证码
            stringRedisTemplate.delete(cacheKey);

            //首先查询该手机号是否已经被绑定了
            User one = lambdaQuery().eq(User::getPhone, phone).one();
            if(one != null){
                throw new BusinessException(AppHttpCodeEnum.PHONE_EXIST);
            }

            User user = new User();
            user.setUid(userInfoVo.getUid());
            user.setPhone(phone);
            //更新数据库
            updateById(user);
            //更新redis
            userInfoVo.setPhone(phone);
            flashRedis(userInfoVo, request);
        } else {
            throw new BusinessException(AppHttpCodeEnum.WRONG_CODE);
        }
    }

    @Override
    public void updateMail(String email, String code, HttpServletRequest request) {
        UserInfoVo userInfoVo = redisUtils.getCurrentUserWithValidation(request);
        //从redis中读取验证码
        String cacheKey = RedisConstants.MAIL_CODE_CACHE_PREFIX + email;
        String cacheCode = stringRedisTemplate.opsForValue().get(cacheKey);
        //验证码校验通过
        if(!StringUtils.isEmpty(cacheCode) && code.equals(cacheCode.split("_")[0])){
            //删除redis中的验证码
            stringRedisTemplate.delete(cacheKey);

            //首先查询该手机号是否已经被绑定了
            User one = lambdaQuery().eq(User::getEmail, email).one();
            if(one != null){
                throw new BusinessException(AppHttpCodeEnum.EMAIL_EXIST);
            }

            User user = new User();
            user.setUid(userInfoVo.getUid());
            user.setEmail(email);
            //更新数据库
            updateById(user);
            //更新redis
            userInfoVo.setEmail(email);
            flashRedis(userInfoVo, request);
        } else {
            throw new BusinessException(AppHttpCodeEnum.WRONG_CODE);
        }
    }

    @Override
    public void loginByPhone(PhoneLoginRequest vo, HttpServletResponse response) {
        //到这里参数校验已经通过
        //从redis中读取验证码
        String cacheKey = RedisConstants.CODE_SMS_CACHE_PREFIX + vo.getPhone();
        String cacheCode = stringRedisTemplate.opsForValue().get(cacheKey);
        //验证码校验通过
        if(!StringUtils.isEmpty(cacheCode) && vo.getCaptcha().equals(cacheCode.split("_")[0])){
            //删除redis中的验证码
            stringRedisTemplate.delete(cacheKey);
            //手机号如果不能存在，则自动注册
            User result = lambdaQuery().eq(User::getPhone, vo.getPhone()).one();
            if(result == null){
                User register = new User();
                register.setPhone(vo.getPhone());
                register.setUsername(USERNAME_PREFIX + UUID.randomUUID());
                save(register);

                UserInfoVo userInfoVo = BeanCopyUtils.copyBean(register, UserInfoVo.class);
                generateTokenAndCookie(userInfoVo, response);
            } else {
                //手机号已经存在，则正常登录
                generateTokenAndCookie(userToVo(result, null), response);
            }
        } else {
            throw new BusinessException(AppHttpCodeEnum.WRONG_CODE);
        }
    }

    @Override
    public UserInfoVo getUserByUid(Long uid, HttpServletRequest request) {
        //todo: 异步编排优化，同时获取当前用户信息和目标用户信息
        User byId = getById(uid);
        if(byId == null){
            throw new BusinessException(AppHttpCodeEnum.NOT_EXIST);
        }

        UserInfoVo currentUser = redisUtils.getCurrentUser(request);

        return userToVo(byId, currentUser);
    }

    @Override
    public List<UserInfoVo> getUserListByUids(List<Long> uids, HttpServletRequest request) {
        if(uids.isEmpty()){
            return new ArrayList<>();
        }
        UserInfoVo currentUser = redisUtils.getCurrentUser(request);
        return getUserListByUids(uids, currentUser);
    }

    /**
     * 根据uids获取userInfoVo，这里重载的目的避免再次利用request去redis查询用户信息
     * @param uids
     * @param currentUser
     * @return
     */
    private List<UserInfoVo> getUserListByUids(List<Long> uids, UserInfoVo currentUser) {
        List<User> users = listByIds(uids);

        int size = users.size();
        UserInfoVo[] vos = new UserInfoVo[size];
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final int index = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                UserInfoVo vo = userToVo(users.get(index), currentUser);
                vos[index] = vo;
            }, threadPoolExecutor);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return Arrays.asList(vos);
    }

    @Override
    public List<RecommendUserVo> getRecommendUsers(HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUser(request);
        Long uid = currentUser == null ? -1L : currentUser.getUid();
        //去redis查询
        double minScore = 0;
        double maxScore = 1;
        String cacheKey = USER_RECOMMEND_PREFIX + uid;

        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(cacheKey, minScore, maxScore);
        if(result.size() == 0){
            return refreshRecommendUsers(request);
        }
        List<Long> recommendUids = result.stream()
                .map(stringTypedTuple -> Long.valueOf(stringTypedTuple.getValue()))
                .collect(Collectors.toList());
        List<Double> scores = result.stream()
                .map(stringTypedTuple -> stringTypedTuple.getScore())
                .collect(Collectors.toList());
        //至此获取了推荐用户的uids和scores
        return getRecommendUserVoList(recommendUids, scores, currentUser);
    }

    @Override
    public List<RecommendUserVo> refreshRecommendUsers(HttpServletRequest request) {
        //不查询redis，要查询数据库获取新的（同时把新的缓存进redis）
        UserInfoVo currentUser = redisUtils.getCurrentUser(request);

        int tagCount = userTagMapper.selectCount(null);
        int userCount = lambdaQuery().count();
        int loopCount = userCount / SystemConstants.RANDOM_RECOMMEND_BATCH_SIZE;

        Map<String, Double> recommendUsersMap;
        if(currentUser == null){
            //随机用户组成的map
            recommendUsersMap = getRandomUserIdsAndCache(tagCount, loopCount);
        } else {
            //特定用户组成的map
            List<Long> tagIds = currentUser.getTags().stream().map(UserTagVo::getId).collect(Collectors.toList());
            recommendUsersMap = getRecommendUserIdsAndCache(currentUser.getUid(), JSON.toJSONString(tagIds), tagCount, loopCount);
        }

        //首先要按照分数排序
        List<ImmutablePair<Long, Double>> pairs = recommendUsersMap.entrySet().stream()
                .sorted((entry1, entry2) -> (int) (10000 * entry2.getValue() - 10000 * entry1.getValue()))
                .map(entry -> new ImmutablePair<Long, Double>(Long.valueOf(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
        List<Long> recommendUids = pairs.stream().map(ImmutablePair::getKey).collect(Collectors.toList());
        List<Double> scores = pairs.stream().map(ImmutablePair::getValue).collect(Collectors.toList());
        return getRecommendUserVoList(recommendUids, scores, currentUser);
    }

    @Override
    public List<UsernameAndAvtarDto> listUserNameAndAvatarByUids(Collection<Long> uids) {
        if(uids.isEmpty()){
            return new ArrayList<>();
        }
        return lambdaQuery()
                .select(User::getUid, User::getUsername, User::getAvatar)
                .in(User::getUid, uids).list().stream()
                .map(user -> BeanCopyUtils.copyBean(user, UsernameAndAvtarDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UsernameAndAvtarDto getUsernameAndAvatar(Long uid) {
        User user = lambdaQuery()
                .select(User::getUid, User::getUsername, User::getAvatar)
                .eq(User::getUid, uid).one();
        return BeanCopyUtils.copyBean(user, UsernameAndAvtarDto.class);
    }

    @Override
    public void checkPhoneUnique(String phone) throws BusinessException {
        Integer phoneCount = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (phoneCount > 0) {
            throw new BusinessException(AppHttpCodeEnum.PHONE_EXIST);
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws BusinessException {
        Integer usernameCount = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (usernameCount > 0) {
            throw new BusinessException(AppHttpCodeEnum.USER_EXIST);
        }
    }

    @Override
    public void checkEmailUnique(String email) throws BusinessException {
        Integer emailCount = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (emailCount > 0) {
            throw new BusinessException(AppHttpCodeEnum.USER_EXIST);
        }
    }

    @Override
    public Map<String, Double> getRecommendUserIdsAndCache(Long uid, String tags, int tagCount, int loopCount){
        int i = 0;
        //存储结果的用户id和对应的分数
        Map<String, Double> result = new HashMap<>();

        while (i < loopCount && result.size() < SystemConstants.RECOMMEND_SIZE && !tags.equals("[]")) {
            i++;
            //随机获取50个没关注的用户
            List<User> randomRecommend = baseMapper.getRandomRecommend(uid, SystemConstants.RANDOM_RECOMMEND_BATCH_SIZE);
            //依次计算，超过阈值就放进res
            for (User recommendUser : randomRecommend) {
                double score = AlgorithmUtils.calculate(tags, recommendUser.getTags(), tagCount);
                if (score > SystemConstants.RECOMMEND_THRESHOLD) {
                    result.put(recommendUser.getUid().toString(), score);
                }
                //放满了就break
                if (result.size() == SystemConstants.RECOMMEND_SIZE) {
                    break;
                }
            }
        }

        //有可能loopSize达到了还没放满（基本把用户表遍历了1遍），那就随机取
        if (result.size() < SystemConstants.RECOMMEND_SIZE) {
            List<User> randomRecommends = baseMapper.getRandomRecommend(uid, SystemConstants.RECOMMEND_SIZE - result.size());
            randomRecommends.forEach(recommendUser -> {
                double score = AlgorithmUtils.calculate(tags, recommendUser.getTags(), tagCount);
                result.put(recommendUser.getUid().toString(), score);
            });
        }

        //将其缓存到redis中（只缓存id，因为考虑到这个数量极大）
        cacheRecommendUids(uid, result);

        return result;
    }

    @Override
    public Map<String, Double> getRandomUserIdsAndCache(int tagCount, int loopCount) {
        //存储结果的用户id和对应的分数
        Map<String, Double> result = new HashMap<>();

        List<User> randomRecommends = baseMapper.getRandom(SystemConstants.RECOMMEND_SIZE);
        randomRecommends.forEach(recommendUser -> {
            result.put(recommendUser.getUid().toString(), 0.0);
        });

        //用-1来代表未登录用户
        cacheRecommendUids(-1L, result);
        return result;
    }

    private void cacheRecommendUids(Long uid, Map<String, Double> result){
        // 执行缓存逻辑
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byte[] keyBytes = stringRedisTemplate.getStringSerializer().serialize(RedisConstants.USER_RECOMMEND_PREFIX + uid);
            connection.del(keyBytes);
            result.forEach((key, value) -> {
                byte[] valueBytes = stringRedisTemplate.getStringSerializer().serialize(key);
                connection.zAdd(keyBytes, value, valueBytes);
            });
            connection.expire(keyBytes, 172800);
            return null;
        });
    }

    private UserInfoVo userToVo(User user, UserInfoVo currentUser) {
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user, UserInfoVo.class);
        List<UserTagVo> userTagVos = userTagService.idsToTags(user.getTags());
        userInfoVo.setTags(userTagVos);

        if(currentUser != null){
            Follow follow = followMapper.selectOne(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getUid, currentUser.getUid()).eq(Follow::getFollowUid, user.getUid()));
            if(follow != null){
                userInfoVo.setIsFollow(true);
            }
        }
        return userInfoVo;
    }

    private void generateTokenAndCookie(UserInfoVo userInfoVo, HttpServletResponse response){
        //生成用户的token，保存至redis，有效期30天
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String token = bCryptPasswordEncoder.encode(userInfoVo.getUid().toString());
        try {
            stringRedisTemplate.opsForValue().set(USER_SESSION_PREFIX + token,
                    MAPPER.writeValueAsString(userInfoVo), USER_SESSION_TTL, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "JSON转换异常");
        }
        //设置cookie
        Cookie cookie = new Cookie(TOKEN, token);
        cookie.setDomain(domain);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(2592000);
        response.addCookie(cookie);
    }

    private void flashRedis(UserInfoVo userInfoVo, HttpServletRequest request){
        try {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.USER_SESSION_PREFIX + HttpUtils.getToken(request),
                    MAPPER.writeValueAsString(userInfoVo), USER_SESSION_TTL, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "JSON转化异常");
        }
    }

    /**
     * 根据uids和scores获取推荐用户
     * @param recommendUids
     * @param scores
     * @param currentUser
     * @return
     */
    private List<RecommendUserVo> getRecommendUserVoList(List<Long> recommendUids, List<Double> scores, UserInfoVo currentUser){
        //首先将uids转为userInfoVo
        List<UserInfoVo> vos = getUserListByUids(recommendUids, currentUser);
        List<RecommendUserVo> recommendUserVos = new ArrayList<>();
        for (int i = 0; i < recommendUids.size(); i++) {
            RecommendUserVo recommendUserVo = new RecommendUserVo();
            recommendUserVo.setScore(scores.get(i) * 100);
            recommendUserVo.setUserInfo(vos.get(i));
            recommendUserVos.add(recommendUserVo);
        }
        return recommendUserVos;
    }
}