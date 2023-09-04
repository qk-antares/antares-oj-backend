package com.antares.oj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.oj.model.entity.ProblemSubmit;
import com.antares.oj.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.oj.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.oj.service.ProblemSubmitService;
import com.antares.oj.utils.UserUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
    @Resource
    private UserFeignService userFeignService;

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
    public R<ProblemSubmit> doProblemSubmit(@RequestBody @NotNull @Valid ProblemSubmitAddRequest problemSubmitAddRequest) {
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        //未登录
        if(currentUser == null){
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }
        ProblemSubmit submitResult = problemSubmitService.doProblemSubmit(problemSubmitAddRequest, currentUser);
        return R.ok(submitResult);
    }

    /**
     * 分页获取题目提交历史
     * @param problemSubmitQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    public R<Page<ProblemSubmitVo>> listProblemSubmitVoByPage(@RequestBody ProblemSubmitQueryRequest problemSubmitQueryRequest) {
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        if(currentUser == null) {
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }
        Page<ProblemSubmitVo> page = problemSubmitService.listProblemSubmitVoByPage(problemSubmitQueryRequest, currentUser.getUid());
        // 返回脱敏信息
        return R.ok(page);
    }

    /**
     * 获取某次历史提交的详细信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<ProblemSubmitVo> getProblemSubmitVoById(@PathVariable("id") Long id) {
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        if(currentUser == null) {
            throw new BusinessException(AppHttpCodeEnum.NOT_LOGIN);
        }
        ProblemSubmit submit = problemSubmitService.getById(id);
        // 返回脱敏信息
        return R.ok(ProblemSubmitVo.objToVo(submit));
    }
}
