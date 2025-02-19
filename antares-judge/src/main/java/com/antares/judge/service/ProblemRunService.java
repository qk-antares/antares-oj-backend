package com.antares.judge.service;

import com.antares.codesandbox.sdk.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.sdk.model.dto.ExecuteCodeRes;

public interface ProblemRunService {
    ExecuteCodeRes doProblemRun(ExecuteCodeReq request);
}
