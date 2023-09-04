package com.antares.oj.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.antares.oj.service.ProblemService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import com.antares.common.constant.CommonConstant;
import com.antares.common.model.vo.UserInfoVo;
import com.antares.common.utils.SqlUtils;
import com.antares.oj.feign.UserFeignService;
import com.antares.oj.mapper.ProblemMapper;
import com.antares.oj.mapper.ProblemSubmitMapper;
import com.antares.oj.model.dto.problem.JudgeConfig;
import com.antares.oj.model.dto.problem.ProblemQueryRequest;
import com.antares.oj.model.entity.Problem;
import com.antares.oj.model.entity.ProblemSubmit;
import com.antares.oj.model.enums.ProblemSubmitStatusEnum;
import com.antares.oj.model.vo.problem.SafeProblemVo;
import com.antares.oj.utils.UserUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Antares
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2023-08-24 10:36:35
*/
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem>
    implements ProblemService {
    @Resource
    private ProblemSubmitMapper problemSubmitMapper;
    @Resource
    private UserFeignService userFeignService;

    @Override
    public Wrapper<Problem> getQueryWrapper(ProblemQueryRequest problemQueryRequest) {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();

        String sortField = problemQueryRequest.getSortField();
        String sortOrder = problemQueryRequest.getSortOrder();
        String keyword = problemQueryRequest.getKeyword();
        String status = problemQueryRequest.getStatus();
        String difficulty = problemQueryRequest.getDifficulty();
        List<String> tags = problemQueryRequest.getTags();

        //不查询content和answer，因为很多时候不显示
        queryWrapper.select(Problem.class, item -> !item.getColumn().equals("content") && !item.getColumn().equals("answer"));

        if(StrUtil.isNotBlank(status) && !status.equals("全部")){
            UserInfoVo currentUser = UserUtils.getCurrentUser(userFeignService.getCurrentUser());
            Set<Long> passedIds;
            Set<Long> triedIds;

            switch (status){
                case "已通过":
                    passedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                                    .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, currentUser.getUid())
                                    .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    if(passedIds.isEmpty()){
                        return null;
                    }
                    queryWrapper.in("id", passedIds);
                    break;
                case "尝试过":
                    passedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                                    .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, currentUser.getUid())
                                    .eq(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    triedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                                    .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, currentUser.getUid())
                                    .ne(ProblemSubmit::getStatus, ProblemSubmitStatusEnum.SUCCEED.getValue()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    triedIds = (Set<Long>) CollUtil.subtract(triedIds, passedIds);
                    if(triedIds.isEmpty()){
                        return null;
                    }
                    queryWrapper.in("id", triedIds);
                    break;
                case "未开始":
                    triedIds = problemSubmitMapper.selectList(new LambdaQueryWrapper<ProblemSubmit>()
                                    .select(ProblemSubmit::getProblemId).eq(ProblemSubmit::getUserId, currentUser.getUid()))
                            .stream().map(ProblemSubmit::getProblemId).collect(Collectors.toSet());
                    if(!triedIds.isEmpty()){
                        queryWrapper.notIn("id", triedIds);
                    }
                    break;
            }
        }

        // 拼接查询条件
        boolean likeQuery = StringUtils.isNotBlank(keyword);
        queryWrapper.like(likeQuery, "title", keyword);
        queryWrapper.like(likeQuery, "content", keyword);
        queryWrapper.like(likeQuery, "answer", keyword);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(StringUtils.isNotBlank(difficulty), "difficulty", difficulty);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public SafeProblemVo objToVo(Problem problem, Long uid) {
        if (problem == null) {
            return null;
        }
        SafeProblemVo safeProblemVO = new SafeProblemVo();
        BeanUtils.copyProperties(problem, safeProblemVO);
        safeProblemVO.setTags(JSONUtil.toList(problem.getTags(), String.class));
        safeProblemVO.setJudgeConfig(JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class));

        //查询当前用户历史做题信息（已通过、尝试过、未开始）
        ProblemSubmit submit = problemSubmitMapper.selectOne(new QueryWrapper<ProblemSubmit>()
                .select("max(status) as status").lambda()
                .eq(ProblemSubmit::getProblemId, problem.getId())
                .eq(ProblemSubmit::getUserId, uid));

        if(submit == null){
            safeProblemVO.setStatus("未开始");
        } else if(submit.getStatus().equals(ProblemSubmitStatusEnum.SUCCEED.getValue())) {
            safeProblemVO.setStatus("已通过");
        } else if(submit.getStatus().equals(ProblemSubmitStatusEnum.FAILED.getValue())){
            safeProblemVO.setStatus("尝试过");
        } else {
            safeProblemVO.setStatus("未开始");
        }

        return safeProblemVO;
    }

    @Override
    public List<String> getProblemTags() {
        return lambdaQuery().select(Problem::getTags).list().stream()
                .flatMap(problem -> JSONUtil.toList(problem.getTags(), String.class).stream())
                .distinct().collect(Collectors.toList());
    }
}




