package com.antares.common.service.judge;

import com.antares.common.model.dto.problemrun.ProblemRunRequest;
import com.antares.common.model.vo.problemrun.ProblemRunResult;

public interface ProblemRunService {
    ProblemRunResult doProblemRun(ProblemRunRequest problemRunRequest, String token);
}
