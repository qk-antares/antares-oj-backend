package com.antares.judge.service;

import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.vo.problemsubmit.ProblemSubmitVo;

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
    ProblemSubmitVo doJudge(ProblemSubmit problemSubmit, String accessKey, String secretKey);
}
