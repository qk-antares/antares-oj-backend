package com.antares.common.service.judge;

import com.antares.common.model.dto.problemrun.ProblemRunRequest;
import com.antares.common.model.vo.problemrun.ProblemRunResult;
import com.antares.common.model.vo.user.UserVo;

public interface ProblemRunService {
    ProblemRunResult doProblemRun(ProblemRunRequest problemRunRequest, UserVo currentUser);
}
