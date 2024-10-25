package com.antares.common.model.vo.user;

import java.io.Serializable;
import java.util.List;

import com.antares.common.model.entity.User;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

@Data
public class UserVo implements Serializable {
    private Long uid;
    private String username;
    private String email;
    private String userRole;
    private List<String> tags;
    private String signature;
    private Integer sex;
    private String avatar;

    public static UserVo userToVo(User user) {
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        userVo.setTags(JSONUtil.toList(user.getTags(), String.class));
        return userVo;
    }
}