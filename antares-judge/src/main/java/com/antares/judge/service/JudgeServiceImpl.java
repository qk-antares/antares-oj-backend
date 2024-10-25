package com.antares.judge.service;


import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeRequest;
import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteCodeResponse;
import com.antares.codesandbox.sdk.model.dto.executecode.ExecuteResult;
import com.antares.codesandbox.sdk.model.enums.ExecuteCodeStatusEnum;
import com.antares.common.mapper.ProblemMapper;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.model.dto.problem.JudgeCase;
import com.antares.common.model.dto.problem.JudgeConfig;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.common.model.enums.judge.JudgeInfoEnum;
import com.antares.common.model.enums.judge.ProblemSubmitStatusEnum;
import com.antares.common.model.vo.problemsubmit.JudgeInfo;
import com.antares.common.service.judge.JudgeService;
import com.antares.common.utils.ThrowUtils;
import com.antares.judge.codesandbox.CodeSandbox;
import com.antares.judge.codesandbox.CodeSandboxFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.json.JSONUtil;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private ProblemMapper problemMapper;
    @Resource
    private ProblemSubmitMapper problemSubmitMapper;
    @Resource
    private CodeSandboxFactory codeSandboxFactory;

    @Value("${antares.code-sandbox.type:remote}")
    private String type;

    @Override
    public ProblemSubmit doJudge(ProblemSubmit problemSubmit, String accessKey, String secretKey) {
        // 1、获取对应的problem
        Problem problem = problemMapper.selectOne(new QueryWrapper<Problem>()
                .select(Problem.class, item -> !item.getColumn().equals("content") && !item.getColumn().equals("answer"))
                .lambda().eq(Problem::getId, problemSubmit.getProblemId()));
        ThrowUtils.throwIf(problem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");

        // 2、更改判题（题目提交）的状态为"判题中"，防止重复执行
        ProblemSubmit updateSubmit = new ProblemSubmit();
        updateSubmit.setId(problemSubmit.getId());
        updateSubmit.setStatus(ProblemSubmitStatusEnum.RUNNING.getValue());
        int update = problemSubmitMapper.updateById(updateSubmit);
        ThrowUtils.throwIf(update == 0, HttpCodeEnum.INTERNAL_SERVER_ERROR, "题目状态更新失败");

        // 3、调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = codeSandboxFactory.newInstance(type);
        String language = problemSubmit.getLanguage();
        String code = problemSubmit.getCode();
        // 获取输入用例
        List<JudgeCase> judgeCaseList = JSONUtil.toList(problem.getJudgeCase(), JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse response = codeSandbox.executeCode(executeCodeRequest, accessKey, secretKey);

        // 4、根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfo judgeInfo = new JudgeInfo();
        int total = judgeCaseList.size();
        judgeInfo.setTotal(total);
        //执行成功
        if(response.getCode().equals(ExecuteCodeStatusEnum.SUCCESS.getValue())){
            //期望输出
            List<String> expectedOutput = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
            //测试用例详细信息
            List<ExecuteResult> results = response.getResults();
            //实际输出
            List<String> output = results.stream().map(ExecuteResult::getOutput).collect(Collectors.toList());
            //判题配置
            JudgeConfig judgeConfig = JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class);

            //设置通过的测试用例
            int pass = 0;
            //设置最大实行时间
            long maxTime = Long.MIN_VALUE;
            for (int i = 0; i < total; i++) {
                //判断执行时间
                Long time = results.get(i).getTime();
                if(time > maxTime){
                    maxTime = time;
                }
                if(expectedOutput.get(i).equals(output.get(i))){
                    //超时
                    if(maxTime > judgeConfig.getTimeLimit()){
                        judgeInfo.setTime(maxTime);
                        judgeInfo.setPass(pass);
                        judgeInfo.setStatus(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getValue());
                        judgeInfo.setMessage(JudgeInfoEnum.TIME_LIMIT_EXCEEDED.getText());
                        break;
                    } else {
                        pass++;
                    }
                } else {
                    //遇到了一个没通过的
                    judgeInfo.setPass(pass);
                    judgeInfo.setTime(maxTime);
                    judgeInfo.setStatus(JudgeInfoEnum.WRONG_ANSWER.getValue());
                    judgeInfo.setMessage(JudgeInfoEnum.WRONG_ANSWER.getText());
                    //设置输出和预期输出信息
                    judgeInfo.setInput(inputList.get(i));
                    judgeInfo.setOutput(output.get(i));
                    judgeInfo.setExpectedOutput(expectedOutput.get(i));
                    break;
                }
            }
            if(pass == total){
                judgeInfo.setPass(total);
                judgeInfo.setTime(maxTime);
                judgeInfo.setStatus(JudgeInfoEnum.ACCEPTED.getValue());
                judgeInfo.setMessage(JudgeInfoEnum.ACCEPTED.getText());
            }
        } else if(response.getCode().equals(ExecuteCodeStatusEnum.RUN_FAILED.getValue())){
            judgeInfo.setPass(0);
            judgeInfo.setStatus(JudgeInfoEnum.RUNTIME_ERROR.getValue());
            judgeInfo.setMessage(JudgeInfoEnum.RUNTIME_ERROR.getText() + response.getMsg());
        } else if(response.getCode().equals(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())){
            judgeInfo.setPass(0);
            judgeInfo.setStatus(JudgeInfoEnum.COMPILE_ERROR.getValue());
            judgeInfo.setMessage(JudgeInfoEnum.COMPILE_ERROR.getText() + response.getMsg());
        }

        // 5、修改数据库中的判题结果
        boolean judgeResult = judgeInfo.getStatus().equals(JudgeInfoEnum.ACCEPTED.getValue());

        updateSubmit.setStatus(judgeResult ?
                ProblemSubmitStatusEnum.SUCCEED.getValue() :
                ProblemSubmitStatusEnum.FAILED.getValue());
        updateSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = problemSubmitMapper.updateById(updateSubmit);
        ThrowUtils.throwIf(update == 0, HttpCodeEnum.INTERNAL_SERVER_ERROR, "题目状态更新失败");

        // 6、修改题目的通过数
        if(judgeResult){
            //将problem的通过数+1
            problemMapper.update(null, new UpdateWrapper<Problem>()
                    .setSql("accepted_num = accepted_num + 1").eq("id", problem.getId()));
        }

        return updateSubmit;
    }
}
