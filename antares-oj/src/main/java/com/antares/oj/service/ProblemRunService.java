package com.antares.oj.service;

import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.model.dto.problemrun.ProblemRunRequest;
import com.antares.oj.model.vo.problemrun.ProblemRunResult;

public interface ProblemRunService {
    ProblemRunResult doProblemRun(ProblemRunRequest problemRunRequest, UserInfoVo currentUser);
}
