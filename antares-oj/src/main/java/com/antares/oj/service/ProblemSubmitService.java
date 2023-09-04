package com.antares.oj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.oj.model.entity.ProblemSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.antares.oj.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.oj.model.vo.problemsubmit.SubmitSummaryVo;

/**
* @author Antares
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-08-24 10:36:35
*/
public interface ProblemSubmitService extends IService<ProblemSubmit> {

    ProblemSubmit doProblemSubmit(ProblemSubmitAddRequest problemSubmitAddRequest, UserInfoVo currentUser);

    Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryRequest problemSubmitQueryRequest, Long uid);

    SubmitSummaryVo getSubmitSummary();
}
