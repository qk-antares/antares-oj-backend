package com.antares.oj.service.impl;

import com.antares.oj.service.ProblemSubmitService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.model.enums.ProblemDifficultyEnum;
import com.antares.oj.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.oj.service.JudgeService;
import com.antares.oj.mapper.ProblemMapper;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitAddRequest;
import com.antares.oj.model.dto.problemsubmit.ProblemSubmitQueryRequest;
import com.antares.oj.model.entity.Problem;
import com.antares.oj.model.entity.ProblemSubmit;
import com.antares.oj.model.enums.LanguageEnum;
import com.antares.oj.model.enums.ProblemSubmitStatusEnum;
import com.antares.oj.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.oj.mapper.ProblemSubmitMapper;
import com.antares.oj.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
    private UserFeignService userFeignService;

    @Override
    public ProblemSubmit doProblemSubmit(ProblemSubmitAddRequest problemSubmitAddRequest, UserInfoVo currentUser) {
        // 校验编程语言是否合法
        String language = problemSubmitAddRequest.getLanguage();
        LanguageEnum languageEnum = LanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(AppHttpCodeEnum.PARAMS_ERROR, "不支持的编程语言");
        }
        long problemId = problemSubmitAddRequest.getProblemId();
        // 判断实体是否存在，根据类别获取实体
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new BusinessException(AppHttpCodeEnum.NOT_EXIST);
        }

        //判断用户是否有正在等待或判题的题，如果有，提交判题失败
        Long userId = currentUser.getUid();
        ProblemSubmit submit = lambdaQuery().eq(ProblemSubmit::getUserId, userId)
                .and(wrapper -> wrapper.eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.WAITING).or()
                        .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.RUNNING)).one();
        if(submit != null){
            throw new BusinessException(AppHttpCodeEnum.SUBMIT_ERROR, "提交过于频繁！");
        }

        //将problem的提交数+1
        problemMapper.update(null, new UpdateWrapper<Problem>()
                .setSql("submit_num = submit_num + 1").eq("id", problem.getId()));

        // 是否已提交题目
        // 每个用户串行提交题目
        ProblemSubmit problemSubmit = new ProblemSubmit();
        problemSubmit.setUserId(userId);
        problemSubmit.setProblemId(problemId);
        problemSubmit.setCode(problemSubmitAddRequest.getCode());
        problemSubmit.setLanguage(language);
        // 设置初始状态
        problemSubmit.setStatus(ProblemSubmitStatusEnum.WAITING.getValue());
        problemSubmit.setJudgeInfo("{}");
        boolean save = this.save(problemSubmit);
        if (!save){
            throw new BusinessException(AppHttpCodeEnum.INTERNAL_SERVER_ERROR, "数据插入失败");
        }
        // 执行判题服务
        ProblemSubmit submitResult = judgeService.doJudge(problemSubmit, currentUser.getAccessKey(), currentUser.getSecretKey());
        return submitResult;
    }

    @Override
    public Page<ProblemSubmitVo> listProblemSubmitVoByPage(ProblemSubmitQueryRequest problemSubmitQueryRequest, Long uid) {
        long pageNum = problemSubmitQueryRequest.getPageNum();
        long pageSize = problemSubmitQueryRequest.getPageSize();
        Long problemId = problemSubmitQueryRequest.getProblemId();
        Page<ProblemSubmit> page = page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProblemSubmit>().select(ProblemSubmit.class, item -> !item.getColumn().equals("code"))
                .eq(ProblemSubmit::getUserId, uid).eq(ProblemSubmit::getProblemId, problemId));

        Page<ProblemSubmitVo> voPage = new Page<>(pageNum, pageSize, page.getTotal());
        List<ProblemSubmitVo> records = page.getRecords().stream().map(ProblemSubmitVo::objToVo).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public SubmitSummaryVo getSubmitSummary() {
        SubmitSummaryVo summaryVo = new SubmitSummaryVo();

        UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());

        //获取简单、中等、困难题目ids
        List<Long> easyIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                        .select(Problem::getId).eq(Problem::getDifficulty, ProblemDifficultyEnum.EASY.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        List<Long> mediumIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                        .select(Problem::getId).eq(Problem::getDifficulty, ProblemDifficultyEnum.MEDIUM.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        List<Long> hardIds = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                        .select(Problem::getId).eq(Problem::getDifficulty, ProblemDifficultyEnum.HARD.getValue()))
                .stream().map(Problem::getId).collect(Collectors.toList());
        int easyTotal = easyIds.size();
        int mediumTotal = mediumIds.size();
        int hardTotal = hardIds.size();
        summaryVo.setEasyTotal(easyTotal);
        summaryVo.setMediumTotal(mediumTotal);
        summaryVo.setHardTotal(hardTotal);
        summaryVo.setTotal(easyTotal + mediumTotal + hardTotal);

        //获取用户通过的简单、中等、困难题目数
        Integer easyPass = baseMapper.getPassCount(currentUser.getUid(), easyIds);
        Integer mediumPass = baseMapper.getPassCount(currentUser.getUid(), mediumIds);
        Integer hardPass = baseMapper.getPassCount(currentUser.getUid(), hardIds);
        summaryVo.setEasyPass(easyPass);
        summaryVo.setMediumPass(mediumPass);
        summaryVo.setHardPass(hardPass);

        //获取用户提交总数
        Integer submitCount = baseMapper.selectCount(new LambdaQueryWrapper<ProblemSubmit>()
                .eq(ProblemSubmit::getUserId, currentUser.getUid()));
        summaryVo.setSubmitCount(submitCount);
        //获取用户成功的提交
        Integer passCount = baseMapper.selectCount(new LambdaQueryWrapper<ProblemSubmit>()
                .eq(ProblemSubmit::getUserId, currentUser.getUid())
                .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()));
        summaryVo.setPassCount(passCount);

        return summaryVo;
    }
}




