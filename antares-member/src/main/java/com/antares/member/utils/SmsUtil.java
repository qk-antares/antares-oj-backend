package com.antares.member.utils;

import com.alibaba.fastjson.JSON;
import com.zhenzi.sms.ZhenziSmsClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "antares.third-party.sms")
public class SmsUtil {
    private String apiUrl;
    private String appId;
    private String appSecret;
    private String templateId;

    public void sendCode(String phone, String code) {
        //发送验证码
    }

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    public void sendCodeTrue(String phone, String code) {
        try {
            //将验证码通过榛子云接口发送至手机
            ZhenziSmsClient client = new ZhenziSmsClient(apiUrl, appId, appSecret);

            //发送短信
            Map<String, Object> params = new HashMap<String, Object>();//参数需要通过Map传递
            params.put("number", phone);
            params.put("templateId", templateId);

            String[] templateParams = new String[2];
            templateParams[0] = code;
            templateParams[1] = "10分钟";
            params.put("templateParams", templateParams);

            String result = client.send(params);
            Map map = JSON.parseObject(result, Map.class);
            int status = (int) map.get("code");
            if(status == 0){
                log.info("短信发送成功！手机号：{}，验证码：{}", phone, code);
            }
        } catch (Exception e) {
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "短信发送失败");
        }
    }
}
