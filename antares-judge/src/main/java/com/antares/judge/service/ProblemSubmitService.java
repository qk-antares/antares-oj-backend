package com.antares.judge.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Future;

import com.antares.judge.model.dto.problemsubmit.ProblemSubmitAddReq;
import com.antares.judge.model.dto.problemsubmit.ProblemSubmitQueryReq;
import com.antares.judge.model.entity.ProblemSubmit;
import com.antares.judge.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.judge.model.vo.problemsubmit.SubmitSummaryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Antares
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2023-08-24 10:36:35
*/
public interface ProblemSubmitService extends IService<ProblemSubmit> {

    ProblemSubmitVo doProblemSubmit(ProblemSubmitAddReq problemSubmitAddReq);

    Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryReq problemSubmitQueryReq);

    SubmitSummaryVo getSubmitSummary(Long uid);

    List<String> getCheckInDatesByMysql(Long uid, String date);

    List<String> getCheckInDatesByRedis(Long uid, String date);

    Future<Boolean> checkInAsync(Long uid, LocalDate date);
}
