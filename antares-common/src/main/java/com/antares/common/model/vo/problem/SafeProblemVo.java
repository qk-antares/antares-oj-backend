package com.antares.common.model.vo.problem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.antares.common.mapper.ProblemSubmitMapper;
import com.antares.common.model.dto.problem.JudgeConfig;
import com.antares.common.model.entity.Problem;
import com.antares.common.model.entity.ProblemSubmit;
import com.antares.common.model.enums.judge.ProblemSubmitStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * 题目封装类
 * @TableName question
 */
@Data
public class SafeProblemVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 状态（已通过、尝试过、未开始）
     */
    private String status;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题解
     */
    private String answer;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;


    public static SafeProblemVo objToVo(Problem problem, Long uid, ProblemSubmitMapper problemSubmitMapper) {
        if (problem == null) {
            return null;
        }
        SafeProblemVo safeProblemVO = new SafeProblemVo();
        BeanUtil.copyProperties(problem, safeProblemVO);
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
}