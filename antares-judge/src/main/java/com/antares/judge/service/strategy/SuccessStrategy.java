package com.antares.judge.service.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.sdk.model.dto.ExecuteResult;
import com.antares.judge.model.dto.problem.JudgeConfig;
import com.antares.judge.model.enums.JudgeInfoEnum;
import com.antares.judge.model.vo.problemsubmit.JudgeInfo;

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

        // 记录通过的测试用例
        int pass = 0;
        // 记录最大执行时间
        long maxTime = Long.MIN_VALUE;
        // 记录最大内存占用
        long maxMemory = Long.MIN_VALUE;
        for (int i = 0; i < total; i++) {
            // 判断执行时间
            Long time = results.get(i).getTime();
            if (time > maxTime) {
                maxTime = time;
            }
            // 判断内存占用
            Long memory = results.get(i).getMemory();
            if (memory > maxMemory) {
                maxMemory = memory;
            }
            if (StrUtil.equals(expectedOutputList.get(i), outputList.get(i))) {
                // 超时
                if (maxTime > judgeConfig.getTimeLimit()) {
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setPass(pass);
                    judgeInfo.setStatus(JudgeInfoEnum.TIMEOUT.getValue());
                    judgeInfo.setMsg(JudgeInfoEnum.TIMEOUT.getMsg());
                    return judgeInfo;
                } else if (maxMemory > judgeConfig.getMemoryLimit() * 1024 * 1024) {
                    // 内存超限
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setPass(pass);
                    judgeInfo.setStatus(JudgeInfoEnum.RUNTIME_ERROR.getValue());
                    judgeInfo.setMsg("超出内存限制");
                    return judgeInfo;
                } else {
                    pass++;
                }
            } else {
                // 遇到了一个没通过的
                judgeInfo.setPass(pass);
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
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setStatus(JudgeInfoEnum.ACCEPTED.getValue());
        judgeInfo.setMsg(JudgeInfoEnum.ACCEPTED.getMsg());

        return judgeInfo;
    }

}
