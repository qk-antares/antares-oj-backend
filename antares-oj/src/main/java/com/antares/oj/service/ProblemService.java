package com.antares.oj.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.antares.oj.model.dto.problem.ProblemQueryRequest;
import com.antares.oj.model.entity.Problem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.antares.oj.model.vo.problem.SafeProblemVo;

import java.util.List;

/**
* @author Antares
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-08-24 10:36:35
*/
public interface ProblemService extends IService<Problem> {
    Wrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest);

    /**
     * 对象转包装类
     * @param problem
     * @return
     */
    SafeProblemVo objToVo(Problem problem, Long uid);

    List<String> getProblemTags();
}
