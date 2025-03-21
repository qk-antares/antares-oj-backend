package com.antares.judge.service.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.sdk.model.dto.ExecuteResult;
import com.antares.common.model.dto.problem.JudgeConfig;
import com.antares.common.model.enums.judge.JudgeInfoEnum;
import com.antares.common.model.vo.problemsubmit.JudgeInfo;

import cn.hutool.core.util.StrUtil;

@Component
public class SuccessStrategy implements ResStrategy {
    @Override
    public JudgeInfo processExecuteCodeRes(ExecuteCodeRes executeCodeRes, List<String> inputList,
            List<String> expectedOutputList, JudgeConfig judgeConfig) {
        JudgeInfo judgeInfo = new JudgeInfo();
        int total = inputList.size();
        judgeInfo.setTotal(total);

        // 测试用例详细信息
        List<ExecuteResult> results = executeCodeRes.getResults();
        // 实际输出
        List<String> outputList = results.stream().map(executeResult -> executeResult.getStdout().trim())
                .collect(Collectors.toList());

        // 设置通过的测试用例
        int pass = 0;
        // 设置最大执行时间
        long maxTime = Long.MIN_VALUE;
        for (int i = 0; i < total; i++) {
            // 判断执行时间
            Long time = results.get(i).getTime();
            if (time > maxTime) {
                maxTime = time;
            }
            if (StrUtil.equals(expectedOutputList.get(i), outputList.get(i))) {
                // 超时
                if (maxTime > judgeConfig.getTimeLimit()) {
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setPass(pass);
                    judgeInfo.setTime(maxTime);
                    judgeInfo.setStatus(JudgeInfoEnum.TIMEOUT.getValue());
                    judgeInfo.setMsg(JudgeInfoEnum.TIMEOUT.getMsg());
                    return judgeInfo;
                } else {
                    pass++;
                }
            } else {
                // 遇到了一个没通过的
                judgeInfo.setPass(pass);
                judgeInfo.setTime(maxTime);
                judgeInfo.setStatus(JudgeInfoEnum.WRONG_ANSWER.getValue());
                judgeInfo.setMsg(JudgeInfoEnum.WRONG_ANSWER.getMsg());
                // 设置输出和预期输出信息
                judgeInfo.setInput(inputList.get(i));
                judgeInfo.setOutput(StrUtil.trim(outputList.get(i)));
                judgeInfo.setExpectedOutput(expectedOutputList.get(i));
                return judgeInfo;
            }
        }
        judgeInfo.setPass(total);
        judgeInfo.setTime(maxTime);
        judgeInfo.setStatus(JudgeInfoEnum.ACCEPTED.getValue());
        judgeInfo.setMsg(JudgeInfoEnum.ACCEPTED.getMsg());

        return judgeInfo;
    }

}
