package com.antares.member.controller;

import com.qiniu.util.Auth;
import lombok.Data;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@Data
@RequestMapping(value = "/member/oss")
@ConfigurationProperties(prefix = "antares.third-party.oss")
public class OSSController {
    // 访问授权码
    private String accessKey;
    // 秘密钥匙
    private String secretKey;
    // 空间名称
    private String bucket;
    // 外链域名
    private String domain;

    @GetMapping("/policy")
    public R<Map<String, Object>> policy(){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            //验证七牛云身份是否通过
            Auth auth = Auth.create(accessKey, secretKey);
            //生成凭证
            String upToken = auth.uploadToken(bucket);
            result.put("token", upToken);
            //存入外链默认域名，用于拼接完整的资源外链路径
            result.put("domain", domain);

            //生成文件夹名
            String dir = "blog/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            result.put("dir", dir);

            result.put("success", 1);
            return R.ok(result);
        } catch (Exception e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "获取凭证失败，"+e.getMessage());
        }
    }
}
