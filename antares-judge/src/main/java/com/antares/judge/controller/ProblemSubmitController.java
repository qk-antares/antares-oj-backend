package com.antares.judge.controller;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.TokenCheck;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.common.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.common.service.judge.ProblemSubmitService;
import com.antares.common.utils.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Antares
 * @date 2023/8/24 17:07
 * @description 题目提交接口
 */
@RestController
@RequestMapping("/oj/problem_submit")
@Slf4j
@Validated
public class ProblemSubmitController {
    @Resource
    private ProblemSubmitService problemSubmitService;

    @GetMapping("/summary")
    public R<SubmitSummaryVo> getSubmitSummary(){
        SubmitSummaryVo vo = problemSubmitService.getSubmitSummary();
        return R.ok(vo);
    }

    /**
     * 提交题目
     * @param problemSubmitAddRequest
     * @return 提交记录的 id
     */
    @PostMapping
    @TokenCheck
    public R<ProblemSubmit> doProblemSubmit(@RequestBody @NotNull @Valid ProblemSubmitAddRequest problemSubmitAddRequest, HttpServletRequest request) {
        ProblemSubmit submitResult = problemSubmitService.doProblemSubmit(problemSubmitAddRequest, null);
        return R.ok(submitResult);
    }

    /**
     * 分页获取题目提交历史
     * @param problemSubmitQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @TokenCheck
    public R<Page<ProblemSubmitVo>> listProblemSubmitVoByPage(@RequestBody ProblemSubmitQueryRequest problemSubmitQueryRequest, HttpServletRequest request) {
        Page<ProblemSubmitVo> page = problemSubmitService.listProblemSubmitVoByPage(problemSubmitQueryRequest, null);
        // 返回脱敏信息
        return R.ok(page);
    }

    /**
     * 获取某次历史提交的详细信息
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
}
