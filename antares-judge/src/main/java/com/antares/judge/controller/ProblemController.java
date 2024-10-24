package com.antares.judge.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.antares.common.annotation.RoleCheck;
import com.antares.common.constant.UserConstant;
import com.antares.common.exception.BusinessException;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.model.dto.problem.ProblemAddRequest;
import com.antares.common.model.dto.problem.ProblemQueryRequest;
import com.antares.common.model.dto.problem.ProblemUpdateRequest;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.common.model.vo.problem.ProblemVo;
import com.antares.common.model.vo.problem.SafeProblemVo;
import com.antares.common.service.judge.ProblemService;
import com.antares.common.service.user.LoginService;
import com.antares.common.utils.R;
import com.antares.common.utils.ThrowUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

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

    @DubboReference
    private LoginService loginService;

    @Resource
    private ProblemSubmitMapper problemSubmitMapper;

    /**
     * 创建
     * @param problemAddRequest
     * @return
     */
    @PostMapping
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Long> addQuestion(@RequestBody @NotNull @Valid ProblemAddRequest problemAddRequest,
                               HttpServletRequest request) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(problemAddRequest, problem);
        //设置创建者
        // UserVo currentUser = loginService.getCurrentUser(request);
        problem.setUserId(1L);
        //设置其他信息
        problem.setTags(JSON.toJSONString(problemAddRequest.getTags()));
        problem.setJudgeCase(JSON.toJSONString(problemAddRequest.getJudgeCase()));
        problem.setJudgeConfig(JSON.toJSONString(problemAddRequest.getJudgeConfig()));
        
        boolean result = problemService.save(problem);
        ThrowUtils.throwIf(!result, HttpCodeEnum.INTERNAL_SERVER_ERROR);
        return R.ok(problem.getId());
    }

    /**
     * 分页获取题目列表（仅管理员）
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
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
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
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
        ThrowUtils.throwIf(oldProblem == null, HttpCodeEnum.NOT_EXIST);
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
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<ProblemVo> getProblemVoById(@PathVariable("id") @Min(1) Long id, HttpServletRequest request) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(HttpCodeEnum.NOT_EXIST);
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
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Boolean> deleteProblem(@PathVariable("id") @Min(1) Long id) {
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, HttpCodeEnum.NOT_EXIST);
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
    public R<SafeProblemVo> getSafeProblemVoById(@PathVariable("id") @Min(1) Long id, HttpServletRequest request) {
        Problem problem = problemService.getById(id);
        if (problem == null) {
            throw new BusinessException(HttpCodeEnum.NOT_EXIST);
        }
        // UserVo currentUser = userService.getCurrentUser(request);
        SafeProblemVo vo = SafeProblemVo.objToVo(problem, 1L, problemSubmitMapper);
        return R.ok(vo);
    }

    /**
     * 分页获取列表（封装类）
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo/safe")
    public R<Page<SafeProblemVo>> listSafeProblemVoByPage(@RequestBody ProblemQueryRequest problemQueryRequest, HttpServletRequest request) {
        long pageNum = problemQueryRequest.getPageNum();
        long pageSize = problemQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, HttpCodeEnum.PARAMS_ERROR);

        Wrapper<Problem> queryWrapper = problemService.getQueryWrapper(problemQueryRequest);
        if(queryWrapper != null){
            Page<Problem> problemPage = problemService.page(new Page<>(pageNum, pageSize), queryWrapper);

            // UserVo currentUser = userService.getCurrentUser(request);
            List<SafeProblemVo> records = problemPage.getRecords().stream()
                    .map(problem -> SafeProblemVo.objToVo(problem, 1L, problemSubmitMapper))
                    .collect(Collectors.toList());
            Page<SafeProblemVo> page = new Page<>(pageNum, pageSize, problemPage.getTotal());
            page.setRecords(records);
            return R.ok(page);
        } else {
            Page<SafeProblemVo> page = new Page<>(pageNum, pageSize, 0);
            return R.ok(page);
        }
    }
}
