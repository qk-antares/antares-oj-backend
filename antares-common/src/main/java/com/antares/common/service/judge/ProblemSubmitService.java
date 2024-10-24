package com.antares.common.service.judge;

import com.antares.common.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.common.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.common.model.vo.user.UserVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Antares
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-08-24 10:36:35
*/
public interface ProblemSubmitService extends IService<ProblemSubmit> {

    ProblemSubmit doProblemSubmit(ProblemSubmitAddRequest problemSubmitAddRequest, UserVo currentUser);

    Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryRequest problemSubmitQueryRequest, Long uid);

    SubmitSummaryVo getSubmitSummary();
}
