package com.antares.codesandbox.service;

import com.antares.codesandbox.model.dto.ExecuteCodeReq;
import com.antares.codesandbox.model.dto.ExecuteCodeRes;

public interface SandboxService {
    ExecuteCodeRes execute(ExecuteCodeReq executeCodeRequest);
}
