package com.antares.codesandbox.sdk.model.dto.executecode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Antares
 * @date 2023/8/26 23:05
 * @description 进程执行信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteResult {
    //退出码
    private Integer exitValue;
    //正常信息
    private String output;
    //错误信息
    private String errorOutput;
    //运行时间
    private Long time;
    //消耗内存
    private Long memory;
}
