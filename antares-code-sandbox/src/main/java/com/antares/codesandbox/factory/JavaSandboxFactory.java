package com.antares.codesandbox.factory;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.template.SandboxTemplate;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.HttpCodeEnum;

@Component
public class JavaSandboxFactory implements SandboxFactory {
    @Resource
    private SandboxTemplate javaNativeAcmSandbox;
    @Resource
    private SandboxTemplate javaDockerAcmSandbox;

    @Override
    public SandboxTemplate createSandboxTemplate(String type) {
        switch (type) {
            case "native":
                return javaNativeAcmSandbox;
            case "docker":
                return javaDockerAcmSandbox;
            default:
                throw new BusinessException(HttpCodeEnum.BAD_REQUEST, "不支持Java的代码沙箱类型: " + type);
        }   
    }
}