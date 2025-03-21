package com.antares.codesandbox.template.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.model.dto.ExecuteResult;
import com.antares.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.antares.codesandbox.model.enums.ExitCodeEnum;
import com.antares.codesandbox.template.SandboxTemplate;
import com.antares.codesandbox.utils.ProcessUtils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

@Component("javaAcmSandbox")
// 只在开启native代码沙箱时加载
@ConditionalOnProperty(name = "antares.code-sandbox.type", havingValue = "native")
@Slf4j
public class JavaNativeAcmSandbox extends SandboxTemplate {
    @Override
    protected ExecuteCodeRes compileAndRun(File codeFile, List<String> inputList) throws IOException {
        // 1. 编译代码
        String[] compileCmd = { "javac", "-encoding", "utf-8", codeFile.getAbsolutePath() };
        log.info("执行命令：{}", Arrays.toString(compileCmd));

        // 创建并执行编译process
        Process compileProcess = Runtime.getRuntime().exec(compileCmd);
        // 拿到编译process执行信息
        ExecuteResult compileResult = ProcessUtils.getProcessMsg(compileProcess);
        if (compileResult.getExitCode() != 0) {
            return ExecuteCodeRes.builder()
                    .code(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())
                    .msg(compileResult.getStderr().replaceAll(codeFile.getParent(), ""))
                    .build();
        }

        // 2. 执行代码
        List<ExecuteResult> executeResults = new ArrayList<>();

        if (CollectionUtil.isEmpty(inputList)) {
            inputList = new ArrayList<>();
            inputList.add(null);
        }

        for (String input : inputList) {
            ExecuteResult executeResult = runOneCase(codeFile, input);
            executeResults.add(executeResult);

            // 已经有用例失败了
            if (executeResult.getExitCode() != ExitCodeEnum.SUCCESS.getValue()) {
                ExecuteCodeStatusEnum statusEnum = ExecuteCodeStatusEnum
                        .getEnumByExitCodeEnum(ExitCodeEnum.getEnumByValue(executeResult.getExitCode()));
                return ExecuteCodeRes.builder()
                        .code(statusEnum.getValue())
                        .msg(executeResult.getStderr())
                        .results(executeResults)
                        .build();
            }
        }

        return ExecuteCodeRes.builder()
                .code(ExecuteCodeStatusEnum.SUCCESS.getValue())
                .msg(ExecuteCodeStatusEnum.SUCCESS.getMsg())
                .results(executeResults)
                .build();
    }

    public ExecuteResult runOneCase(File codeFile, String input) throws IOException {
        String[] runCmd = { "java", String.format("-Xmx%dm", this.javaXmx), "-Dfile.encoding=UTF-8",
                "-cp", codeFile.getParent(), "Main" };

        long start = System.currentTimeMillis();

        Process runProcess = Runtime.getRuntime().exec(runCmd);
        // 超时控制
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(this.timeout);
                // 超时了，直接杀死运行代码的进程
                runProcess.destroy();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        ExecuteResult executeResult = null;
        if (input == null) {
            executeResult = ProcessUtils.getProcessMsg(runProcess);
        } else {
            executeResult = ProcessUtils.getProcessMessage(runProcess, input);
        }

        long end = System.currentTimeMillis();
        executeResult.setTime(end - start);

        if (!thread.isAlive()) {
            executeResult = new ExecuteResult();
            executeResult.setExitCode(ExitCodeEnum.TIMEOUT.getValue());
            executeResult.setStderr("超出时间限制");
        }

        return executeResult;
    }
}