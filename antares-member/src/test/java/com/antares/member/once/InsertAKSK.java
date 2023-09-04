package com.antares.member.once;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.antares.member.mapper.UserMapper;
import com.antares.member.model.entity.User;
import com.antares.member.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class InsertAKSK {
    @Resource
    private UserService userService;

    @Test
    public void insetAKSK(){
        List<User> list = userService.list();
        for (User user : list) {
            user.setAccessKey(DigestUtil.sha1Hex(RandomUtil.randomString(32)));
            user.setSecretKey(DigestUtil.sha1Hex(RandomUtil.randomString(32)));
        }
        userService.updateBatchById(list, 100);
    }
}
