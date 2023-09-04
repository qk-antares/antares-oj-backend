package com.antares.member.job;

import lombok.extern.slf4j.Slf4j;
import com.antares.common.constant.SystemConstants;
import com.antares.member.mapper.UserTagMapper;
import com.antares.member.model.entity.User;
import com.antares.member.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Antares
 * @date 2023/5/13 15:41
 * @description 生成推荐用户(5个)并保存至redis，方便用户访问，执行频率是每天1次
 */
@Component
@Slf4j
public class RecommendJob {
    @Resource
    private UserService userService;
    @Resource
    private UserTagMapper userTagMapper;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 要用sorted set存储（后续应该1天执行一次）
     */
    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void getRecommendUsersJob(){
        long start = System.currentTimeMillis();

        int tagCount = userTagMapper.selectCount(null);
        List<User> userList = userService.lambdaQuery().select(User::getUid, User::getTags).list();
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();

        int loopCount = userList.size() / SystemConstants.RANDOM_RECOMMEND_BATCH_SIZE;
        for (User user : userList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.getRecommendUserIdsAndCache(user.getUid(), user.getTags(), tagCount, loopCount);
            }, threadPoolExecutor);
            futures.add(future);
        }

        //缓存一个未登录用户，uid为-1
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            userService.getRandomUserIdsAndCache(tagCount, loopCount);
        }, threadPoolExecutor);
        futures.add(future);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();
        System.out.println("执行耗时:" + (end - start));
    }
}
