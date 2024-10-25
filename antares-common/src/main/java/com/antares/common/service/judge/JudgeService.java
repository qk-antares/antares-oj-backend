package com.antares.common.service.judge;

import com.antares.common.model.entity.ProblemSubmit;

/**
 * @author Antares
 * @date 2023/8/26 15:31
 * @description 判题服务，抽象出微服务
 */
public interface JudgeService {

    /**
     * 判题
     * 
     * @param problemSubmit
     * @return
     */
    ProblemSubmit doJudge(ProblemSubmit problemSubmit, String accessKey, String secretKey);
}
