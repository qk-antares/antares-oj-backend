package com.antares.common.service.judge;

import java.util.List;

import com.antares.common.model.dto.problem.ProblemAddRequest;
import com.antares.common.model.dto.problem.ProblemQueryRequest;
import com.antares.common.model.dto.problem.ProblemUpdateRequest;
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
    Long addProblem(ProblemAddRequest problemAddRequest, String token);

    Page<ProblemVo> listProblemVoByPage(ProblemQueryRequest problemQueryRequest);

    void updateProblem(ProblemUpdateRequest problemUpdateRequest);

    Page<SafeProblemVo> listSafeProblemVoByPage(ProblemQueryRequest problemQueryRequest, String token);

    List<String> getProblemTags();
}
