package com.antares.judge.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.antares.common.constant.SqlConstant;
import com.antares.common.mapper.ProblemMapper;
import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.model.dto.problem.ProblemQueryRequest;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.enums.judge.ProblemSubmitStatusEnum;
import com.antares.common.service.judge.ProblemService;
import com.antares.common.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * @author Antares
 * @description 针对表【problem(题目)】的数据库操作Service实现
 * @createDate 2024-10-15 19:32:36
 */
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
        implements ProblemService {
    @Resource
    private ProblemSubmitMapper problemSubmitMapper;

    @Override
    public Wrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();

        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        String keyword = problemQueryRequest.getKeyword();
        String status = problemQueryRequest.getStatus();
        String difficulty = problemQueryRequest.getDifficulty();
        List<String> tags = problemQueryRequest.getTags();

        // 不查询content和answer，因为很多时候不显示
        queryWrapper.select(Problem.class,
                item -> !item.getColumn().equals("content") && !item.getColumn().equals("answer"));

        if (StrUtil.isNotBlank(status) && !status.equals("全部")) {
            Set<Long> passedIds;
            Set<Long> triedIds;

            switch (status) {
                case "已通过":
                    passedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                            .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, 1L)
                            .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    if (passedIds.isEmpty()) {
                        return null;
                    }
                    queryWrapper.in("id", passedIds);
                    break;
                case "尝试过":
                    passedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                            .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, 1L)
                            .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    triedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                            .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, 1L)
                            .ne(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    triedIds = (Set<Long>) CollUtil.subtract(triedIds, passedIds);
                    if (triedIds.isEmpty()) {
                        return null;
                    }
                    queryWrapper.in("id", triedIds);
                    break;
                case "未开始":
                    triedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                            .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, 1L))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    if (!triedIds.isEmpty()) {
                        queryWrapper.notIn("id", triedIds);
                    }
                    break;
            }
        }

        // 拼接查询条件
        boolean likeQuery = StrUtil.isNotBlank(keyword);
        queryWrapper.like(likeQuery, "title", keyword);
        queryWrapper.like(likeQuery, "content", keyword);
        queryWrapper.like(likeQuery, "answer", keyword);
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(StrUtil.isNotBlank(difficulty), "difficulty", difficulty);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(SqlConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public List<String> getProblemTags() {
        return lambdaQuery().select(Problem::getTags).list().stream()
                .flatMap(problem -> JSONUtil.toList(problem.getTags(), String.class).stream())
                .distinct().collect(Collectors.toList());
    }

}
