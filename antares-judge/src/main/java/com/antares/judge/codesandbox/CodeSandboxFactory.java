package com.antares.judge.codesandbox;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;
import com.antares.judge.codesandbox.impl.RemoteCodeSandbox;
import com.antares.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * @author Antares
 * @date 2023/9/3 8:24
 * @description 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例）
 */
@Component
public class CodeSandboxFactory {
    @Resource
    private RemoteCodeSandbox remoteCodeSandbox;
    @Resource
    private ThirdPartyCodeSandbox thirdPartyCodeSandbox;

    /**
     * 创建代码沙箱示例
     *
     * @param apiProvider 沙箱类型
     * @return
     */
    public CodeSandbox newInstance(String apiProvider) {
        switch (apiProvider) {
            case "remote":
                return remoteCodeSandbox;
            case "thirdParty":
                return thirdPartyCodeSandbox;
            default:
                throw new BusinessException(HttpCodeEnum.INTERNAL_SERVER_ERROR, "不支持的代码沙箱接口：" + apiProvider);
        }
    }
}
