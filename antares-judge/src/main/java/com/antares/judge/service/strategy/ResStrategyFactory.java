package com.antares.judge.service.strategy;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.sdk.model.enums.ExecuteCodeStatusEnum;

@Component
public class ResStrategyFactory {
    @Resource
    private ResStrategy successStrategy;
    @Resource
    private ResStrategy compileFailedStrategy;
    @Resource
    private ResStrategy timeoutStrategy;
    @Resource
    private ResStrategy runFailedStrategy;

    public ResStrategy getStrategy(ExecuteCodeStatusEnum anEnum) {
        switch (anEnum) {
            case ExecuteCodeStatusEnum.SUCCESS:
                return successStrategy;
            case ExecuteCodeStatusEnum.COMPILE_FAILED:   
                return compileFailedStrategy;
            case ExecuteCodeStatusEnum.TIMEOUT:
                return timeoutStrategy;
            case ExecuteCodeStatusEnum.RUN_FAILED:
                return runFailedStrategy;        
            default:
                return null;
        }
    }
    
}
