package com.antares.judge.controller;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;
import com.antares.common.auth.annotation.TokenCheck;
import com.antares.common.core.dto.R;
import com.antares.judge.service.ProblemRunService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/run")
@Slf4j
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
    public R<ExecuteCodeRes> doProblemRun(@RequestBody @NotNull @Valid ExecuteCodeReq request) {
        ExecuteCodeRes response = problemRunService.doProblemRun(request);
        return R.ok(response);
    }
}
