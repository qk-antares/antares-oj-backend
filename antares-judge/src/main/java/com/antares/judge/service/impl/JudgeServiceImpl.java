package com.antares.judge.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.sdk.model.enums.ExecuteCodeStatusEnum;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.utils.ThrowUtils;
import com.antares.judge.codesandbox.CodeSandbox;
import com.antares.judge.codesandbox.CodeSandboxFactory;
import com.antares.judge.mapper.ProblemMapper;
import com.antares.judge.mapper.ProblemSubmitMapper;
import com.antares.judge.model.dto.problem.JudgeCase;
import com.antares.judge.model.dto.problem.JudgeConfig;
import com.antares.judge.model.entity.Problem;
import com.antares.judge.model.entity.ProblemSubmit;
import com.antares.judge.model.enums.JudgeInfoEnum;
import com.antares.judge.model.enums.ProblemSubmitStatusEnum;
import com.antares.judge.model.vo.problemsubmit.JudgeInfo;
import com.antares.judge.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.judge.service.JudgeService;
import com.antares.judge.service.strategy.ResStrategyFactory;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private ProblemMapper problemMapper;
    @Resource
    private ProblemSubmitMapper problemSubmitMapper;
    @Resource
    private CodeSandboxFactory codeSandboxFactory;
    @Resource
    private ResStrategyFactory resStrategyFactory;

    @Value("${antares.judge.api-provider:remote}")
    private String apiProvider;

    @Override
    public ProblemSubmitVo doJudge(ProblemSubmit problemSubmit, String accessKey, String secretKey) {
        // 1、获取对应的problem
        Problem problem = problemMapper.selectOne(new QueryWrapper<Problem>()
                .select(Problem.class,
                        item -> !item.getColumn().equals("content")
                                && !item.getColumn().equals("answer"))
                .lambda().eq(Problem::getId, problemSubmit.getProblemId()));
        ThrowUtils.throwIf(problem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");

        // 2、更改判题（题目提交）的状态为"判题中"，防止重复执行
        ProblemSubmit updateSubmit = new ProblemSubmit();
        updateSubmit.setId(problemSubmit.getId());
        updateSubmit.setStatus(ProblemSubmitStatusEnum.RUNNING.getValue());
        int update = problemSubmitMapper.updateById(updateSubmit);
        ThrowUtils.throwIf(update == 0, HttpCodeEnum.INTERNAL_SERVER_ERROR, "题目状态更新失败");

        // 3、调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = codeSandboxFactory.newInstance(apiProvider);
        String language = problemSubmit.getLanguage();
        String code = problemSubmit.getCode();
        // 获取输入用例
        List<JudgeCase> judgeCaseList = JSONUtil.toList(problem.getJudgeCase(), JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        // 期望输出
        List<String> expectedOutput = judgeCaseList.stream().map(judgeCase -> judgeCase.getOutput().trim())
                .collect(Collectors.toList());
        // 判题配置
        JudgeConfig judgeConfig = JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class);
        ExecuteCodeReq executeCodeRequest = ExecuteCodeReq.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeRes response = codeSandbox.executeCode(executeCodeRequest, accessKey, secretKey);

        // 4、将代码沙箱返回的响应转成JudgeInfo (工厂+策略模式消除if-else)
        JudgeInfo judgeInfo = resStrategyFactory
                .getStrategy(ExecuteCodeStatusEnum.getEnumByValue(response.getCode()))
                .processExecuteCodeRes(response, inputList, expectedOutput, judgeConfig);

        // 5、修改数据库中的判题结果
        boolean submitStatus = judgeInfo.getStatus().equals(JudgeInfoEnum.ACCEPTED.getValue());
        updateSubmit.setStatus(
                submitStatus ? ProblemSubmitStatusEnum.SUCCEED.getValue() : ProblemSubmitStatusEnum.FAILED.getValue());
        updateSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = problemSubmitMapper.updateById(updateSubmit);
        ThrowUtils.throwIf(update == 0, HttpCodeEnum.INTERNAL_SERVER_ERROR, "题目状态更新失败");

        // 6、修改题目的通过数
        if (submitStatus) {
            // 将problem的通过数+1
            problemMapper.update(null, new UpdateWrapper<Problem>()
                    .setSql("accepted_num = accepted_num + 1").eq("id", problem.getId()));
        }

        ProblemSubmitVo vo = BeanUtil.copyProperties(problemSubmit, ProblemSubmitVo.class, "status", "judgeInfo");
        vo.setStatus(updateSubmit.getStatus());
        vo.setJudgeInfo(judgeInfo);

        return vo;
    }
}
