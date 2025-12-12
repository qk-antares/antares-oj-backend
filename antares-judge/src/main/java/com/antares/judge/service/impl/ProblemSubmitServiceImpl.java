package com.antares.judge.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.antares.common.auth.utils.TokenUtils;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;
import com.antares.common.core.utils.ThrowUtils;
import com.antares.common.redis.constant.RedisConstant;
import com.antares.judge.mapper.ProblemMapper;
import com.antares.judge.mapper.ProblemSubmitMapper;
import com.antares.judge.model.dto.problemsubmit.ProblemSubmitAddReq;
import com.antares.judge.model.dto.problemsubmit.ProblemSubmitQueryReq;
import com.antares.judge.model.entity.Problem;
import com.antares.judge.model.entity.ProblemSubmit;
import com.antares.judge.model.enums.LanguageEnum;
import com.antares.judge.model.enums.ProblemDifficultyEnum;
import com.antares.judge.model.enums.ProblemSubmitStatusEnum;
import com.antares.judge.model.vo.problemsubmit.PassCountVo;
import com.antares.judge.model.vo.problemsubmit.ProblemSubmitVo;
import com.antares.judge.model.vo.problemsubmit.SubmitSummaryVo;
import com.antares.judge.service.JudgeService;
import com.antares.judge.service.ProblemSubmitService;
import com.antares.user.api.dto.SecretDTO;
import com.antares.user.api.service.UserInnerService;
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
    private Snowflake snowflake;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @DubboReference
    private UserInnerService userInnerService;

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
        SecretDTO secretDTO = userInnerService.getSecretByUid(userId);
        ProblemSubmitVo submitResult = judgeService.doJudge(problemSubmit, secretDTO.getSecretId(),
                secretDTO.getSecretKey());

        // 执行签到
        if (submitResult.getStatus().equals(ProblemSubmitStatusEnum.SUCCEED.getValue())) {
            checkInAsync(userId, LocalDate.now());
        }

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
    public SubmitSummaryVo getSubmitSummary(Long uid) {
        SubmitSummaryVo summaryVo = new SubmitSummaryVo();

        // 获取用户通过的简单、中等、困难题目数
        PassCountVo easyPass = baseMapper.getPassCount(uid, ProblemDifficultyEnum.EASY.getValue());
        PassCountVo mediumPass = baseMapper.getPassCount(uid, ProblemDifficultyEnum.MEDIUM.getValue());
        PassCountVo hardPass = baseMapper.getPassCount(uid, ProblemDifficultyEnum.HARD.getValue());
        summaryVo.setEasyPass(easyPass.getPassCount());
        summaryVo.setEasyTotal(easyPass.getTotalCount());
        summaryVo.setMediumPass(mediumPass.getPassCount());
        summaryVo.setMediumTotal(mediumPass.getTotalCount());
        summaryVo.setHardPass(hardPass.getPassCount());
        summaryVo.setHardTotal(hardPass.getTotalCount());

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

    @Override
    public List<String> getCheckInDatesByMysql(Long uid, String date) {
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1); // 月初
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1); // 下月月初

        // 如果需要字符串格式
        String startDateStr = startDate.toString(); // "2025-12-01"
        String endDateStr = endDate.toString(); // "2026-01-01"

        List<String> checkInDates = baseMapper.getCheckInDates(uid, startDateStr, endDateStr);
        return checkInDates;
    }

    @Override
    public List<String> getCheckInDatesByRedis(Long uid, String date) {
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        int daysInMonth = yearMonth.lengthOfMonth();
        String key = String.format(RedisConstant.CHECK_IN_FORMAT, uid, yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));
        byte[] rawKey = stringRedisTemplate.getStringSerializer().serialize(key);
        byte[] bitmap = stringRedisTemplate.execute((RedisCallback<byte[]>)connection -> connection.get(rawKey));
        if (bitmap == null) {
            return new ArrayList<>();
        }
        List<String> checkInDates = new ArrayList<>();
        for (int i = 0; i < daysInMonth; i++) {
            int byteIndex = i / 8;
            if (byteIndex >= bitmap.length) {
                break;
            }
            int bitIndex = i % 8;
            boolean signed = (bitmap[byteIndex] & (1 << (7 - bitIndex))) != 0;
            if (signed) {
                LocalDate signDate = yearMonth.atDay(i + 1);
                checkInDates.add(signDate.toString());
            }
        }
        return checkInDates;
    }

    @Override
    @Async
    public Future<Boolean> checkInAsync(Long uid, LocalDate date) {
        String key = String.format("checkin:%d:%s", uid, date.format(DateTimeFormatter.ofPattern("yyyyMM")));
        int offset = date.getDayOfMonth() - 1;
        stringRedisTemplate.opsForValue().setBit(key, offset, true);
        return new AsyncResult<>(true);
    }
}
