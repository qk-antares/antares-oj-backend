package com.antares.codesandbox.factory;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.template.SandboxTemplate;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.HttpCodeEnum;

@Component
public class CppSandboxFactory implements SandboxFactory {
    @Resource
    private SandboxTemplate cppNativeAcmSandbox;
    @Resource
    private SandboxTemplate cppDockerAcmSandbox;

    @Override
    public SandboxTemplate createSandboxTemplate(String type) {
        switch (type) {
            case "native":
                return cppNativeAcmSandbox;
            case "docker":
                return cppDockerAcmSandbox;
            default:
                throw new BusinessException(HttpCodeEnum.BAD_REQUEST, "不支持cpp的代码沙箱类型: " + type);
        }
    }
}
