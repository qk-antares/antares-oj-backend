package com.antares.judge.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.common.exception.BusinessException;
import com.antares.common.mapper.ProblemMapper;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.mapper.UserMapper;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitAddReq;
import com.antares.common.model.dto.problemsubmit.ProblemSubmitQueryReq;
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

import cn.hutool.core.lang.Snowflake;

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
    @Resource
    private Snowflake snowflake;

    // TODO 限流，防止用户频繁提交
    @Override
    public ProblemSubmitVo doProblemSubmit(ProblemSubmitAddReq problemSubmitAddReq) {
        // 校验编程语言是否合法
        String language = problemSubmitAddReq.getLanguage();
        LanguageEnum languageEnum = LanguageEnum.getEnumByValue(language);
        ThrowUtils.throwIf(languageEnum == null, new BusinessException(HttpCodeEnum.BAD_REQUEST, "不支持的编程语言"));

        long problemId = problemSubmitAddReq.getProblemId();
        // 将problem的提交数+1
        int rowsAffected = problemMapper.update(new UpdateWrapper<Problem>()
                .setSql("submit_num = submit_num + 1").eq("id", problemId));
        ThrowUtils.throwIf(rowsAffected == 0, new BusinessException(HttpCodeEnum.NOT_EXIST, "题目不存在"));

        // 插入problemSubmit
        Long userId = TokenUtils.getCurrentUid();
        ProblemSubmit problemSubmit = new ProblemSubmit();
        problemSubmit.setId(snowflake.nextId());
        problemSubmit.setUserId(userId);
        problemSubmit.setProblemId(problemId);
        problemSubmit.setCode(problemSubmitAddReq.getCode());
        problemSubmit.setLanguage(language);
        problemSubmit.setCreateTime(new Date());
        // 设置初始状态
        problemSubmit.setStatus(ProblemSubmitStatusEnum.WAITING.getValue());
        problemSubmit.setJudgeInfo("{}");
        boolean save = save(problemSubmit);
        ThrowUtils.throwIf(!save, HttpCodeEnum.INTERNAL_SERVER_ERROR, "提交失败");

        // 执行判题服务
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getAccessKey, User::getSecretKey).eq(User::getUid, userId));
        ProblemSubmitVo submitResult = judgeService.doJudge(problemSubmit, user.getAccessKey(),
                user.getSecretKey());
        return submitResult;
    }

    @Override
    public Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryReq problemSubmitQueryReq) {
        long pageNum = problemSubmitQueryReq.getCurrent();
        long pageSize = problemSubmitQueryReq.getSize();
        Long problemId = problemSubmitQueryReq.getProblemId();
        Long uid = TokenUtils.getCurrentUid();
        Page<ProblemSubmit> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProblemSubmit>()
                        .eq(ProblemSubmit::getUserId, uid)
                        .eq(ProblemSubmit::getProblemId, problemId)
                        .orderByDesc(ProblemSubmit::getCreateTime));

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
