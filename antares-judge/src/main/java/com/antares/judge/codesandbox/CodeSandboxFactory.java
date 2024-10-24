package com.antares.judge.codesandbox;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.antares.judge.codesandbox.impl.ExampleCodeSandbox;
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
    @Resource
    private ExampleCodeSandbox exampleCodeSandbox;

    /**
     * 创建代码沙箱示例
     *
     * @param type 沙箱类型
     * @return
     */
    public CodeSandbox newInstance(String type) {
        switch (type) {
            case "remote":
                return remoteCodeSandbox;
            case "thirdParty":
                return thirdPartyCodeSandbox;
            case "example":
            default:
                return exampleCodeSandbox;
        }
    }
}
