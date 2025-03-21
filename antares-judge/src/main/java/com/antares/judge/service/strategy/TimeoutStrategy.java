package com.antares.judge.service.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.common.model.dto.problem.JudgeConfig;
import com.antares.common.model.enums.judge.JudgeInfoEnum;
import com.antares.common.model.vo.problemsubmit.JudgeInfo;

@Component
public class TimeoutStrategy implements ResStrategy {

    @Override
    public JudgeInfo processExecuteCodeRes(ExecuteCodeRes executeCodeRes, List<String> inputList,
            List<String> expectedOutputList, JudgeConfig judgeConfig) {
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setTotal(inputList.size());
        int pass = executeCodeRes.getResults().size() - 1;
        judgeInfo.setPass(pass);
        // 最后执行的输入
        judgeInfo.setInput(inputList.get(pass));

        judgeInfo.setStatus(JudgeInfoEnum.TIMEOUT.getValue());
        judgeInfo.setMsg(JudgeInfoEnum.TIMEOUT.getMsg());

        return judgeInfo;
    }
    
}
