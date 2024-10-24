package com.antares.codesandbox.controller;


import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.codesandbox.model.dto.ExecuteCodeRequest;
import com.antares.codesandbox.model.dto.ExecuteCodeResponse;
import com.antares.codesandbox.service.SandboxService;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/sandbox")
@Slf4j
@Validated
public class SandboxController {
    @Resource
    private SandboxService sandboxService;

    @PostMapping("/execute")
    public ExecuteCodeResponse execute(@RequestBody ExecuteCodeRequest executeCodeRequest){
        return sandboxService.execute(executeCodeRequest);
    }
}
