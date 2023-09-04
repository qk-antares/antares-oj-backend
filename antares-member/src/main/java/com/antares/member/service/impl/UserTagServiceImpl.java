package com.antares.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.model.vo.UserTagVo;
import com.antares.common.utils.BeanCopyUtils;
import com.antares.common.utils.ObjectMapperUtils;
import com.antares.member.mapper.UserTagMapper;
import com.antares.member.model.dto.tag.UserTagAddRequest;
import com.antares.member.model.entity.UserTag;
import com.antares.member.model.entity.UserTagCategory;
import com.antares.member.model.vo.tag.UserTagCategoryVo;
import com.antares.member.service.UserTagService;
import com.antares.member.utils.RedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.antares.common.constant.RedisConstants.USER_TAGS_CATEGORY;
import static com.antares.common.constant.RedisConstants.USER_TAGS_PREFIX;
import static com.antares.common.constant.SystemConstants.TAG_COLORS;

/**
* @author Antares
* @description 针对表【user_tag】的数据库操作Service实现
* @createDate 2023-03-05 22:05:53
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService{
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public List<UserTagCategoryVo> getAllTags() {
        //获取大类（这个信息是常驻Redis的...，后期可以用）
        List<UserTagCategory> categories = redisUtils.readList(USER_TAGS_CATEGORY, UserTagCategory.class);

        //获取标签
        List<UserTagCategoryVo> userTagCategoryVos = categories.stream().map(userTagCategory -> {
            UserTagCategoryVo userTagCategoryVo = BeanCopyUtils.copyBean(userTagCategory, UserTagCategoryVo.class);
            List<UserTag> tags = redisUtils.readList(USER_TAGS_PREFIX + userTagCategoryVo.getId(), UserTag.class);
            userTagCategoryVo.setTags(tags);
            return userTagCategoryVo;
        }).collect(Collectors.toList());

        return userTagCategoryVos;
    }

    @Override
    public UserTagVo addATag(UserTagAddRequest userTagAddRequest, HttpServletRequest request) {
        UserInfoVo currentUser = redisUtils.getCurrentUserWithValidation(request);

        //获取这个分类下的所有标签
        List<UserTag> tags = redisUtils.readList(USER_TAGS_PREFIX + userTagAddRequest.getParentId(), UserTag.class);

        //判断这个标签是否出现过
        for (UserTag tag : tags) {
            if(tag.getName().equals(userTagAddRequest.getName())){
                throw new BusinessException(AppHttpCodeEnum.USER_TAG_EXIST);
            }
        }

        //标签不存在则将其插入到mysql和redis中（随机生成一个颜色）
        UserTag userTag = BeanCopyUtils.copyBean(userTagAddRequest, UserTag.class);
        userTag.setCreatedBy(currentUser.getUid());
        userTag.setColor(TAG_COLORS[new Random().nextInt(TAG_COLORS.length)]);
        save(userTag);

        //存入redis并返回
        UserTagVo userTagVo = BeanCopyUtils.copyBean(userTag, UserTagVo.class);
        stringRedisTemplate.opsForList().rightPush(USER_TAGS_PREFIX + userTag.getParentId(), ObjectMapperUtils.writeValueAsString(userTagVo));
        return userTagVo;
    }

    @Override
    public List<UserTagVo> idsToTags(String idsJSON){
        List<Long> tagIds = JSON.parseObject(idsJSON, new TypeReference<List<Long>>(){});
        return idsToTags(tagIds);
    }

    @Override
    public List<UserTagVo> idsToTags(List<Long> tagIds){
        if(tagIds.isEmpty()){
            return new ArrayList<>();
        }
        return listByIds(tagIds).stream().map(userTag -> BeanCopyUtils.copyBean(userTag, UserTagVo.class)).collect(Collectors.toList());
    }
}




