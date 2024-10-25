package com.antares.judge.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.annotation.RoleCheck;
import com.antares.common.annotation.TokenCheck;
import com.antares.common.constant.UserConstant;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.model.dto.problem.ProblemAddRequest;
import com.antares.common.model.dto.problem.ProblemQueryRequest;
import com.antares.common.model.dto.problem.ProblemUpdateRequest;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.common.model.vo.problem.ProblemVo;
import com.antares.common.model.vo.problem.SafeProblemVo;
import com.antares.common.service.judge.ProblemService;
import com.antares.common.utils.R;
import com.antares.common.utils.ThrowUtils;
import com.antares.common.utils.TokenUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Antares
 * @date 2023/8/25 9:27
 * @description 题目接口
 */
@RestController
@RequestMapping("/problem")
@Slf4j
@Validated
public class ProblemController {
    @Resource
    private ProblemService problemService;

    @Resource
    private ProblemSubmitMapper problemSubmitMapper;

    /**
     * 创建
     * 
     * @param problemAddRequest
     * @return
     */
    @PostMapping
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Long> addProblem(@RequestBody @NotNull @Valid ProblemAddRequest problemAddRequest,
            @RequestHeader("Authorization") String token) {
        Long id = problemService.addProblem(problemAddRequest, token);
        return R.ok(id);
    }

    /**
     * 分页获取题目列表（仅管理员）
     * 
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo")
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Page<ProblemVo>> listProblemVoByPage(@RequestBody @NotNull ProblemQueryRequest problemQueryRequest) {
        Page<ProblemVo> page = problemService.listProblemVoByPage(problemQueryRequest);
        return R.ok(page);
    }

    /**
     * 更新（仅管理员）
     * 
     * @param problemUpdateRequest
     * @return
     */
    @PutMapping
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Void> updateProblem(@RequestBody @NotNull @Valid ProblemUpdateRequest problemUpdateRequest) {
        problemService.updateProblem(problemUpdateRequest);
        return R.ok();
    }

    /**
     * 根据 id 获取完整信息（仅管理员，包含测试用例）
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}/vo")
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<ProblemVo> getProblemVoById(@PathVariable("id") @Min(1) Long id) {
        Problem problem = problemService.getById(id);
        ThrowUtils.throwIf(problem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");
        ProblemVo problemVo = ProblemVo.objToVo(problem);
        return R.ok(problemVo);
    }

    /**
     * 删除（仅管理员）
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Void> deleteProblem(@PathVariable("id") @Min(1) Long id) {
        // 判断是否存在
        Problem oldProblem = problemService.getById(id);
        ThrowUtils.throwIf(oldProblem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");
        boolean result = problemService.removeById(id);
        ThrowUtils.throwIf(!result, HttpCodeEnum.INTERNAL_SERVER_ERROR, "题目删除失败");
        return R.ok();
    }

    /**
     * 获取所有标签
     * 引入Spring Cache + Redis，实现题目标签的缓存，当管理员插入新题目或更新题目时，更新缓存。
     * Cache不一定要在Controller层，最好在Service层
     * 
     * @return
     */
    @GetMapping("/tags")
    public R<List<String>> getProblemTags() {
        List<String> tags = problemService.getProblemTags();
        return R.ok(tags);
    }

    /**
     * 根据 id 获取（脱敏）
     * 
     * @param id
     * @return
     */
    @GetMapping("/{id}/vo/safe")
    public R<SafeProblemVo> getSafeProblemVoById(@PathVariable("id") @Min(1) Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Problem problem = problemService.getById(id);
        ThrowUtils.throwIf(problem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");
        Long uid = TokenUtils.getUidFromToken(token);
        SafeProblemVo vo = SafeProblemVo.objToVo(problem, uid, problemSubmitMapper);
        return R.ok(vo);
    }

    /**
     * 分页获取列表（脱敏）
     * 
     * @param problemQueryRequest
     * @return
     */
    @PostMapping("/page/vo/safe")
    public R<Page<SafeProblemVo>> listSafeProblemVoByPage(@RequestBody ProblemQueryRequest problemQueryRequest,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Page<SafeProblemVo> page = problemService.listSafeProblemVoByPage(problemQueryRequest, token);
        return R.ok(page);
    }
}
