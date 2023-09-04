package com.antares.member.job;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import com.antares.common.model.vo.UserTagVo;
import com.antares.common.utils.BeanCopyUtils;
import com.antares.member.mapper.UserTagCategoryMapper;
import com.antares.member.mapper.UserTagMapper;
import com.antares.member.model.entity.UserTag;
import com.antares.member.model.entity.UserTagCategory;
import com.antares.member.utils.RedisUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.antares.common.constant.RedisConstants.USER_TAGS_CATEGORY;
import static com.antares.common.constant.RedisConstants.USER_TAGS_PREFIX;

//@Component
@Slf4j
public class StartRunner implements CommandLineRunner {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserTagMapper userTagMapper;
    @Resource
    private UserTagCategoryMapper userTagCategoryMapper;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(String... args) {
        //首先查询所有的类别
        List<UserTagCategory> userTagCategories = userTagCategoryMapper.selectList(null);
        if(CollectionUtils.isNotEmpty(userTagCategories)){
            stringRedisTemplate.delete(USER_TAGS_CATEGORY);
            redisUtils.rightPushAllAsString(USER_TAGS_CATEGORY, userTagCategories);
        }

        //根据类别查询标签
        userTagCategories.stream().forEach(userTagCategory -> {
            LambdaQueryWrapper<UserTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserTag::getParentId, userTagCategory.getId());
            List<UserTagVo> userTags = userTagMapper.selectList(wrapper).stream()
                    .map(userTag -> BeanCopyUtils.copyBean(userTag, UserTagVo.class))
                    .collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userTags)){
                stringRedisTemplate.delete(USER_TAGS_PREFIX + userTagCategory.getId());
                redisUtils.rightPushAllAsString(USER_TAGS_PREFIX + userTagCategory.getId(), userTags);
            }
        });
    }
}