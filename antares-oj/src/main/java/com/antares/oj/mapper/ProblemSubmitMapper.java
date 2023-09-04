package com.antares.oj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.antares.oj.model.entity.ProblemSubmit;

import java.util.List;

/**
* @author Antares
* @description 针对表【question_submit(题目提交)】的数据库操作Mapper
* @createDate 2023-08-24 10:36:35
* @Entity com.antares.oj.model.entity.QuestionSubmit
*/
public interface ProblemSubmitMapper extends BaseMapper<ProblemSubmit> {
    Integer getPassCount(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}




