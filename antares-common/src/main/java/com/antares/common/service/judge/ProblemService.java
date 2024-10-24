package com.antares.common.service.judge;

import java.util.List;

import com.antares.common.model.dto.problem.ProblemQueryRequest;
import com.antares.common.model.entity.Problem;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Antares
* @description 针对表【problem(题目)】的数据库操作Service
* @createDate 2024-10-15 19:32:36
*/
public interface ProblemService extends IService<Problem> {
    Wrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest);

    List<String> getProblemTags();
}
