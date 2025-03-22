package com.antares.common.mapper;

import org.apache.ibatis.annotations.Param;

import com.antares.common.model.entity.Problem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Antares
* @description 针对表【problem(题目)】的数据库操作Mapper
* @createDate 2024-10-15 19:32:36
* @Entity com.antares.common.model.entity.Problem
*/
public interface ProblemMapper extends BaseMapper<Problem> {
    Long getAdjacentProblemId(@Param("id") Long id, @Param("direction") String direction);
}




