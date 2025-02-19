package com.antares.judge.service;

import java.util.List;

import com.antares.common.model.dto.problem.ProblemAddReq;
import com.antares.common.model.dto.problem.ProblemQueryReq;
import com.antares.common.model.dto.problem.ProblemUpdateReq;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.vo.problem.ProblemVo;
import com.antares.common.model.vo.problem.SafeProblemVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Antares
* @description 针对表【problem(题目)】的数据库操作Service
* @createDate 2024-10-15 19:32:36
*/
public interface ProblemService extends IService<Problem> {
    Long addProblem(ProblemAddReq problemAddReq, String token);

    Page<ProblemVo> listProblemVoByPage(ProblemQueryReq problemQueryReq);

    void updateProblem(ProblemUpdateReq problemUpdateReq);

    Page<SafeProblemVo> listSafeProblemVoByPage(ProblemQueryReq problemQueryReq, String token);

    List<String> getProblemTags();
}
