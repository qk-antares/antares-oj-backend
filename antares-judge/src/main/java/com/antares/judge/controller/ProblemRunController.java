package com.antares.judge.controller;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.TokenCheck;
import com.antares.common.model.dto.problemrun.ProblemRunRequest;
import com.antares.common.model.vo.problemrun.ProblemRunResult;
import com.antares.common.service.judge.ProblemRunService;
import com.antares.common.utils.R;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/run")
@Slf4j
@Validated
public class ProblemRunController {
    @Resource
    private ProblemRunService problemRunService;

    /**
     * 在自定义的测试用例上运行代码
     * 
     * @param problemRunRequest
     * @param request
     * @return
     */
    @PostMapping
    @TokenCheck
    public R<ProblemRunResult> doProblemRun(@RequestBody @NotNull @Valid ProblemRunRequest problemRunRequest,
            @RequestHeader("Authorization") String token) {
        ProblemRunResult problemRunResult = problemRunService.doProblemRun(problemRunRequest, token);
        return R.ok(problemRunResult);
    }
}
