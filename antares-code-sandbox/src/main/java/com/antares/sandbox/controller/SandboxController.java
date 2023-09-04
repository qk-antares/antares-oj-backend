package com.antares.sandbox.controller;

import cn.hutool.json.JSONUtil;
import com.antares.sandbox.feign.UserFeignService;
import com.antares.sandbox.model.enums.ExecuteCodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import com.antares.sandbox.model.dto.ExecuteCodeRequest;
import com.antares.sandbox.model.dto.ExecuteCodeResponse;
import com.antares.sandbox.service.SandboxService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.antares.sandbox.utils.SignUtils.genSign;

@RestController
@RequestMapping("/sandbox")
@Slf4j
@Validated
public class SandboxController {
    @Resource
    private SandboxService sandboxService;
    @Resource
    private UserFeignService userFeignService;

    @PostMapping("/execute")
    public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request){
        //首先进行校验
        String accessKey = request.getHeader("accessKey");
        String sign = request.getHeader("sign");
        String secretKey = userFeignService.getSecretKey(accessKey);
        String body = JSONUtil.toJsonStr(executeCodeRequest);
        //使用同样的算法生成签名
        String verify = genSign(body, secretKey);
        if(verify.equals(sign)){
            log.info("校验通过");
            return sandboxService.execute(executeCodeRequest);
        } else {
            return ExecuteCodeResponse.builder()
                    .code(ExecuteCodeStatusEnum.NO_AUTH.getValue())
                    .msg(ExecuteCodeStatusEnum.NO_AUTH.getMsg())
                    .build();
        }
    }
}
