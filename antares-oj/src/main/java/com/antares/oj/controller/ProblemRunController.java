package com.antares.oj.controller;

import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.model.dto.problemrun.ProblemRunRequest;
import com.antares.oj.model.vo.problemrun.ProblemRunResult;
import com.antares.oj.service.ProblemRunService;
import com.antares.oj.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/oj/problem_run")
@Slf4j
@Validated
public class ProblemRunController {
    @Resource
    private UserFeignService userFeignService;
    @Resource
    private ProblemRunService problemRunService;

    @PostMapping
    public R<ProblemRunResult> doProblemRun(@RequestBody @NotNull @Valid ProblemRunRequest problemRunRequest) {
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        //未登录
        if(currentUser == null){
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }

        ProblemRunResult problemRunResult = problemRunService.doProblemRun(problemRunRequest, currentUser);
        return R.ok(problemRunResult);
    }
}
