package com.antares.codesandbox.controller;


import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.service.SandboxService;

import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
public class SandboxController {
    @Resource
    private SandboxService sandboxService;

    @PostMapping("/execute")
    public ExecuteCodeRes execute(@RequestBody ExecuteCodeReq executeCodeRequest){
        return sandboxService.execute(executeCodeRequest);
    }
}
