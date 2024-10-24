package com.antares.common.model.vo.problem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.antares.common.model.dto.problem.JudgeCase;
import com.antares.common.model.dto.problem.JudgeConfig;
import com.antares.common.model.entity.Problem;

import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * 题目封装类
 * @TableName question
 */
@Data
public class ProblemVo implements Serializable {
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
     * 测试用例（json 对象）
     */
    private List<JudgeCase> judgeCase;

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
     * 创建者
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 对象转包装类
     * @param problem
     * @return
     */
    public static ProblemVo objToVo(Problem problem) {
        if (problem == null) {
            return null;
        }
        ProblemVo problemVo = new ProblemVo();
        BeanUtils.copyProperties(problem, problemVo);
        problemVo.setTags(JSONUtil.toList(problem.getTags(), String.class));
        problemVo.setJudgeConfig(JSONUtil.toBean(problem.getJudgeConfig(), JudgeConfig.class));
        problemVo.setJudgeCase(JSONUtil.toList(problem.getJudgeCase(), JudgeCase.class));
        return problemVo;
    }

    private static final long serialVersionUID = 1L;
}