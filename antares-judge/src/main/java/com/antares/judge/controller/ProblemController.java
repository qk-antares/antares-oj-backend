package com.antares.judge.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.auth.annotation.RoleCheck;
import com.antares.common.auth.annotation.TokenCheck;
import com.antares.common.auth.constant.UserConstant;
import com.antares.common.auth.utils.TokenUtils;
import com.antares.common.core.dto.R;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.utils.ThrowUtils;
import com.antares.judge.mapper.ProblemMapper;
import com.antares.judge.mapper.ProblemSubmitMapper;
import com.antares.judge.model.dto.problem.ProblemAddReq;
import com.antares.judge.model.dto.problem.ProblemQueryReq;
import com.antares.judge.model.dto.problem.ProblemUpdateReq;
import com.antares.judge.model.entity.Problem;
import com.antares.judge.model.vo.problem.ProblemVo;
import com.antares.judge.model.vo.problem.SafeProblemVo;
import com.antares.judge.service.ProblemService;
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
public class ProblemController {
    @Resource
    private ProblemService problemService;
    @Resource
    private ProblemSubmitMapper problemSubmitMapper;
    @Resource
    private ProblemMapper problemMapper;

    /**
     * 创建
     * 
     * @param problemAddReq
     * @return
     */
    @PostMapping
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Long> addProblem(@RequestBody @NotNull @Valid ProblemAddReq problemAddReq) {
        Long id = problemService.addProblem(problemAddReq);
        return R.ok(id);
    }

    /**
     * 分页获取题目列表（仅管理员）
     * 
     * @param problemQueryReq
     * @return
     */
    @PostMapping("/page/vo")
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Page<ProblemVo>> listProblemVoByPage(@RequestBody @NotNull ProblemQueryReq problemQueryReq) {
        Page<ProblemVo> page = problemService.listProblemVoByPage(problemQueryReq);
        return R.ok(page);
    }

    /**
     * 更新（仅管理员）
     * 
     * @param problemUpdateReq
     * @return
     */
    @PutMapping
    @TokenCheck
    @RoleCheck(mustRole = UserConstant.ADMIN_ROLE)
    public R<Void> updateProblem(@RequestBody @NotNull @Valid ProblemUpdateReq problemUpdateReq) {
        problemService.updateProblem(problemUpdateReq);
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
    public R<SafeProblemVo> getSafeProblemVoById(@PathVariable("id") @Min(1) Long id) {
        Problem problem = problemService.getById(id);
        ThrowUtils.throwIf(problem == null, HttpCodeEnum.NOT_EXIST, "题目不存在");
        Long uid = TokenUtils.getCurrentUid();
        SafeProblemVo vo = SafeProblemVo.objToVo(problem, uid, problemSubmitMapper);
        return R.ok(vo);
    }

    /**
     * 分页获取列表（脱敏）
     * 
     * @param problemQueryReq
     * @return
     */
    @PostMapping("/page/vo/safe")
    public R<Page<SafeProblemVo>> listSafeProblemVoByPage(@RequestBody ProblemQueryReq problemQueryReq) {
        Page<SafeProblemVo> page = problemService.listSafeProblemVoByPage(problemQueryReq);
        return R.ok(page);
    }

    @GetMapping("/{id}/{direction}")
    public R<Long> getAdjacentProblemId(@PathVariable("id") @Min(1) Long id, @PathVariable("direction") String direction) {
        return R.ok(problemMapper.getAdjacentProblemId(id, direction));
    }
}
