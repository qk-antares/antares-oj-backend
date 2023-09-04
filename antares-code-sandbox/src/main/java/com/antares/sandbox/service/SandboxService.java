package com.antares.sandbox.service;

import com.antares.sandbox.model.dto.ExecuteCodeRequest;
import com.antares.sandbox.model.dto.ExecuteCodeResponse;

public interface SandboxService {
    ExecuteCodeResponse execute(ExecuteCodeRequest executeCodeRequest);
}
