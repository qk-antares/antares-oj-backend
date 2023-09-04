package com.antares.oj.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.utils.ThrowUtils;
import com.antares.oj.annotation.AuthCheck;
import com.antares.oj.constant.UserConstant;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.model.dto.problem.*;
import com.antares.oj.model.entity.Problem;
import com.antares.oj.model.vo.problem.ProblemVo;
import com.antares.oj.model.vo.problem.SafeProblemVo;
import com.antares.oj.service.ProblemService;
import com.antares.oj.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Antares
 * @date 2023/8/25 9:27
 * @description 题目接口
 */
@RestController
@RequestMapping("/oj/problem")
@Slf4j
@Validated
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @Resource
    private UserFeignService userFeignService;

    /**
     * 创建
     * @param problemAddRequest
     * @return
     */
    @PostMapping
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Long> addQuestion(@RequestBody @NotNull @Valid ProblemAddRequest problemAddRequest) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemAddRequest, problem);
        //设置创建者
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        problem.setUserId(currentUser.getUid());
        //设置其他信息
        problem.setTags(JSON.toJSONString(problemAddRequest.getTags()));
        problem.setJudgeCase(JSON.toJSONString(problemAddRequest.getJudgeCase()));
        problem.setJudgeConfig(JSON.toJSONString(problemAddRequest.getJudgeConfig()));

        boolean result = problemService.save(problem);
        ThrowUtils.throwIf(!result, AppHttpCodeEnum.INTERNAL_SERVER_ERROR);
        return R.ok(problem.getId());
    }

    /**
     * 分页获取题目列表（仅管理员）
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Page<ProblemVo>> listProblemVoByPage(@RequestBody @NotNull ProblemQueryRequest problemQueryRequest) {
        long pageNum = problemQueryRequest.getPageNum();
        long pageSize = problemQueryRequest.getPageSize();
        Page<Problem> questionPage = problemService.page(new Page<>(pageNum, pageSize),
                problemService.getQueryWrapper(problemQueryRequest));
        List<ProblemVo> records = questionPage.getRecords().stream().map(ProblemVo::objToVo).collect(Collectors.toList());
        Page<ProblemVo> page = new Page<>(pageNum, pageSize, questionPage.getTotal());
        page.setRecords(records);
        return R.ok(page);
    }

    /**
     * 更新（仅管理员）
     * @param questionUpdateRequest
     * @return
     */
    @PutMapping
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Boolean> updateProblem(@RequestBody @NotNull @Valid ProblemUpdateRequest questionUpdateRequest) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(questionUpdateRequest, problem);
        //设置其他信息
        problem.setTags(JSON.toJSONString(questionUpdateRequest.getTags()));
        problem.setJudgeCase(JSON.toJSONString(questionUpdateRequest.getJudgeCase()));
        problem.setJudgeConfig(JSON.toJSONString(questionUpdateRequest.getJudgeConfig()));

        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, AppHttpCodeEnum.NOT_EXIST);
        boolean result = problemService.updateById(problem);
        return R.ok(result);
    }

    /**
     * 根据 id 获取完整信息（仅管理员，包含测试用例）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/{id}/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<ProblemVo> getProblemVoById(@PathVariable("id") @Min(1) Long id, HttpServletRequest request) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(AppHttpCodeEnum.NOT_EXIST);
        }
        ProblemVo problemVo = ProblemVo.objToVo(problem);
        return R.ok(problemVo);
    }

    /**
     * 删除（仅管理员）
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Boolean> deleteProblem(@PathVariable("id") @Min(1) Long id) {
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, AppHttpCodeEnum.NOT_EXIST);
        boolean result = problemService.removeById(id);
        return R.ok(result);
    }

    /**
     * 获取所有标签
     * @return
     */
    @GetMapping("/tags")
    public R<List<String>> getProblemTags() {
        List<String> tags = problemService.getProblemTags();
        return R.ok(tags);
    }

    /**
     * 根据 id 获取（脱敏）
     * @param id
     * @return
     */
    @GetMapping("/{id}/vo/safe")
    public R<SafeProblemVo> getSafeProblemVoById(@PathVariable("id") @Min(1) Long id) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(AppHttpCodeEnum.NOT_EXIST);
        }
        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
        SafeProblemVo vo = problemService.objToVo(problem, currentUser.getUid());
        return R.ok(vo);
    }

    /**
     * 分页获取列表（封装类）
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo/safe")
    public R<Page<SafeProblemVo>> listSafeProblemVoByPage(@RequestBody ProblemQueryRequest problemQueryRequest) {
        long pageNum = problemQueryRequest.getPageNum();
        long pageSize = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, AppHttpCodeEnum.PARAMS_ERROR);

        Wrapper<Problem> queryWrapper = problemService.getQueryWrapper(problemQueryRequest);
        if(queryWrapper != null){
            Page<Problem> problemPage = problemService.page(new Page<>(pageNum, pageSize), queryWrapper);

            UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
            List<SafeProblemVo> records = problemPage.getRecords().stream()
                    .map(problem -> problemService.objToVo(problem, currentUser.getUid()))
                    .collect(Collectors.toList());
            Page<SafeProblemVo> page = new Page<>(pageNum, pageSize, problemPage.getTotal());
            page.setRecords(records);
            return R.ok(page);
        } else {
            Page<SafeProblemVo> page = new Page<>(pageNum, pageSize, 0);
            return R.ok(page);
        }
    }

    /**
     * 编辑（用户）
     * @param problemEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public R editQuestion(@RequestBody @NotNull @Valid ProblemEditRequest problemEditRequest,
                                   HttpServletRequest request) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemEditRequest, problem);

        // 判断是否存在
        long id = problemEditRequest.getId();
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, AppHttpCodeEnum.NOT_EXIST);

        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());

        // 仅本人或管理员可编辑
        if (!oldProblem.getUserId().equals(currentUser.getUid()) && !UserUtils.isAdmin(currentUser)) {
            throw new BusinessException(AppHttpCodeEnum.NO_AUTH);
        }
        problemService.updateById(problem);
        return R.ok();
    }
}
