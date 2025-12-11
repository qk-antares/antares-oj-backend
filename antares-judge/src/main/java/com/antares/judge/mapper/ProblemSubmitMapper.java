package com.antares.judge.mapper;

import org.apache.ibatis.annotations.Param;

import com.antares.judge.model.entity.ProblemSubmit;
import com.antares.judge.model.vo.problemsubmit.PassCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Antares
* @description 针对表【problem_submit(题目提交)】的数据库操作Mapper
* @createDate 2024-10-16 10:19:54
* @Entity com.antares.common.model.entity.ProblemSubmit
*/
public interface ProblemSubmitMapper extends BaseMapper<ProblemSubmit> {
    PassCountVo getPassCount(@Param("userId") Long userId, @Param("difficulty") String difficulty);
}


