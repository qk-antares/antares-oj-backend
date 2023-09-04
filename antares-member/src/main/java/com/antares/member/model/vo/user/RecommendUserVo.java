package com.antares.member.model.vo.user;

import lombok.Data;
import com.antares.common.model.vo.UserInfoVo;

@Data
public class RecommendUserVo {
    private UserInfoVo userInfo;
    private Double score;
}
