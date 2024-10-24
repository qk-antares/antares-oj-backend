package com.antares.codesandbox.service;

import com.antares.codesandbox.model.dto.ExecuteCodeRequest;
import com.antares.codesandbox.model.dto.ExecuteCodeResponse;

public interface SandboxService {
    ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest);
}
