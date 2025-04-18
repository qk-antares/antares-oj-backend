package com.antares.judge.service.strategy;

import java.util.List;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.judge.model.dto.problem.JudgeConfig;
import com.antares.judge.model.vo.problemsubmit.JudgeInfo;

public interface ResStrategy {
    JudgeInfo processExecuteCodeRes(ExecuteCodeRes executeCodeRes, List<String> inputList, List<String> expectedOutputList, JudgeConfig judgeConfig);    
}
