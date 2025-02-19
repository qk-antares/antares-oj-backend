package com.antares.judge.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.common.exception.BusinessException;
import com.antares.common.mapper.ProblemMapper;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.entity.User;
import com.antares.common.model.enums.HttpCodeEnum;
import com.antares.common.model.enums.judge.LanguageEnum;
import com.antares.common.model.enums.judge.ProblemDifficultyEnum;
import com.antares.common.model.enums.judge.ProblemSubmitStatusEnum;
import com.antares.common.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.common.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.common.utils.ThrowUtils;
import com.antares.common.utils.TokenUtils;
import com.antares.judge.service.JudgeService;
import com.antares.judge.service.ProblemSubmitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @author Antares
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-08-24 10:36:35
 */
@Service
public class ProblemSubmitServiceImpl extends ServiceImpl<ProblemSubmitMapper, ProblemSubmit>
        implements ProblemSubmitService {

    @Resource
    private ProblemMapper problemMapper;
    @Resource
    private JudgeService judgeService;
    @Resource
    private UserMapper userMapper;

    @Override
    public ProblemSubmit doProblemSubmit(ProblemSubmitAddRequest problemSubmitAddRequest, String token) {
        // 校验编程语言是否合法
        String language = problemSubmitAddRequest.getLanguage();
        LanguageEnum languageEnum = LanguageEnum.getEnumByValue(language);
        ThrowUtils.throwIf(languageEnum == null, new BusinessException(HttpCodeEnum.BAD_REQUEST, "不支持的编程语言"));

        long problemId = problemSubmitAddRequest.getProblemId();
        // 判断题目是否存在
        Problem problem = problemMapper.selectById(problemId);
        ThrowUtils.throwIf(problem == null, new BusinessException(HttpCodeEnum.NOT_EXIST, "题目不存在"));

        // 判断用户是否有正在等待或判题的题，如果有，提交判题失败
        Long userId = TokenUtils.getUidFromToken(token);
        ProblemSubmit submit = lambdaQuery().eq(ProblemSubmit::getUserId, userId)
                .lt(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()).one();
        if (submit != null) {
            throw new BusinessException(HttpCodeEnum.SUBMIT_ERROR, "提交过于频繁！");
        }

        // 将problem的提交数+1
        problemMapper.update(new UpdateWrapper<Problem>()
                .setSql("submit_num = submit_num + 1").eq("id", problem.getId()));

        // 插入problemSubmit
        ProblemSubmit problemSubmit = new ProblemSubmit();
        problemSubmit.setUserId(userId);
        problemSubmit.setProblemId(problemId);
        problemSubmit.setCode(problemSubmitAddRequest.getCode());
        problemSubmit.setLanguage(language);
        // 设置初始状态
        problemSubmit.setStatus(ProblemSubmitStatusEnum.WAITING.getValue());
        problemSubmit.setJudgeInfo("{}");
        boolean save = save(problemSubmit);
        ThrowUtils.throwIf(!save, HttpCodeEnum.INTERNAL_SERVER_ERROR, "提交失败");

        // 执行判题服务
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getAccessKey, User::getSecretKey).eq(User::getUid, userId));
        ProblemSubmit submitResult = judgeService.doJudge(problemSubmit, user.getAccessKey(), user.getSecretKey());
        return submitResult;
    }

    @Override
    public Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryRequest problemSubmitQueryRequest,
            Long uid) {
        long pageNum = problemSubmitQueryRequest.getCurrent();
        long pageSize = problemSubmitQueryRequest.getSize();
        Long problemId = problemSubmitQueryRequest.getProblemId();
        Page<ProblemSubmit> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProblemSubmit>()
                        .select(ProblemSubmit.class, item -> !item.getColumn().equals("code"))
                        .eq(ProblemSubmit::getUserId, uid)
                        .eq(ProblemSubmit::getProblemId, problemId));

        Page<ProblemSubmitVo> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ProblemSubmitVo> records = page.getRecords().stream().map(ProblemSubmitVo::objToVo)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public SubmitSummaryVo getSubmitSummary(String token) {
        SubmitSummaryVo summaryVo = new SubmitSummaryVo();

        Long uid = TokenUtils.getUidFromToken(token);

        // 获取简单、中等、困难题目ids
        List<Long> easyIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .select(Problem::getId)
                .eq(Problem::getDifficulty, ProblemDifficultyEnum.EASY.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        List<Long> mediumIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .select(Problem::getId)
                .eq(Problem::getDifficulty, ProblemDifficultyEnum.MEDIUM.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        List<Long> hardIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .select(Problem::getId)
                .eq(Problem::getDifficulty, ProblemDifficultyEnum.HARD.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        int easyTotal = easyIds.size();
        int mediumTotal = mediumIds.size();
        int hardTotal = hardIds.size();
        summaryVo.setEasyTotal(easyTotal);
        summaryVo.setMediumTotal(mediumTotal);
        summaryVo.setHardTotal(hardTotal);
        summaryVo.setTotal(easyTotal + mediumTotal + hardTotal);

        // 获取用户通过的简单、中等、困难题目数
        Integer easyPass = baseMapper.getPassCount(uid, easyIds);
        Integer mediumPass = baseMapper.getPassCount(uid, mediumIds);
        Integer hardPass = baseMapper.getPassCount(uid, hardIds);
        summaryVo.setEasyPass(easyPass);
        summaryVo.setMediumPass(mediumPass);
        summaryVo.setHardPass(hardPass);

        // 获取用户提交总数
        Integer submitCount = baseMapper.selectCount(new LambdaQueryWrapper<ProblemSubmit>()
                .eq(ProblemSubmit::getUserId, uid)).intValue();
        summaryVo.setSubmitCount(submitCount);
        // 获取用户成功的提交
        Integer passCount = baseMapper.selectCount(new LambdaQueryWrapper<ProblemSubmit>()
                .eq(ProblemSubmit::getUserId, uid)
                .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue())).intValue();
        summaryVo.setPassCount(passCount);

        return summaryVo;
    }
}
