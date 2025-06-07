package com.antares.judge.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.auth.annotation.TokenCheck;
import com.antares.common.core.dto.R;
import com.antares.judge.model.dto.problemsubmit.ProblemSubmitAddReq;
import com.antares.judge.model.dto.problemsubmit.ProblemSubmitQueryReq;
import com.antares.judge.model.entity.ProblemSubmit;
import com.antares.judge.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.judge.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.judge.service.ProblemSubmitService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Antares
 * @date 2023/8/24 17:07
 * @description 题目提交接口
 */
@RestController
@RequestMapping("/submit")
@Slf4j
public class ProblemSubmitController {
    @Resource
    private ProblemSubmitService problemSubmitService;

    /**
     * 提交题目
     * 
     * @param problemSubmitAddRequest
     * @return 提交记录的 id
     */
    @PostMapping
    @TokenCheck
    public R<ProblemSubmitVo> doProblemSubmit(
            @RequestBody @NotNull @Valid ProblemSubmitAddReq problemSubmitAddRequest) {
        ProblemSubmitVo submitResult = problemSubmitService.doProblemSubmit(problemSubmitAddRequest);
        return R.ok(submitResult);
    }

    /**
     * 分页获取题目提交历史
     * 
     * @param problemSubmitQueryReq
     * @return
     */
    @PostMapping("/page/vo")
    @TokenCheck
    public R<Page<ProblemSubmitVo>> listProblemSubmitVoByPage(
            @RequestBody ProblemSubmitQueryReq problemSubmitQueryReq) {
        Page<ProblemSubmitVo> page = problemSubmitService.listProblemSubmitVoByPage(problemSubmitQueryReq);
        // 返回脱敏信息
        return R.ok(page);
    }

    /**
     * 获取某次历史提交的详细信息
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @TokenCheck
    public R<ProblemSubmitVo> getProblemSubmitVoById(@PathVariable("id") Long id, HttpServletRequest request) {
        ProblemSubmit submit = problemSubmitService.getById(id);
        // 返回脱敏信息
        return R.ok(ProblemSubmitVo.objToVo(submit));
    }

    /**
     * 获取提交统计
     * 
     * @return
     */
    @GetMapping("/summary")
    @TokenCheck
    public R<SubmitSummaryVo> getSubmitSummary() {
        SubmitSummaryVo vo = problemSubmitService.getSubmitSummary();
        return R.ok(vo);
    }

}
