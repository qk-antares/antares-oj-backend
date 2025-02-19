package com.antares.codesandbox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

import com.antares.codesandbox.model.dto.ExecuteResult;

import lombok.extern.slf4j.Slf4j;

/**
 * 进程工具类
 */
@Slf4j
public class ProcessUtils {
    /**
     * 获取进程执行信息
     * 
     * @param process
     * @return
     */
    public static ExecuteResult getProcessMsg(Process process) {
        ExecuteResult executeResult = new ExecuteResult();

        try {
            // 计时
            long start = System.currentTimeMillis();

            // 等待程序执行，获取退出码
            int exitValue = process.waitFor();

            executeResult.setExitCode(exitValue);
            executeResult.setStderr(getProcessOutput(process.getErrorStream()));
            executeResult.setStdout(getProcessOutput(process.getInputStream()));

            long end = System.currentTimeMillis();
            executeResult.setTime(end - start);
        } catch (Exception e) {
            log.error("编译失败：{}", e.toString());
        }
        return executeResult;
    }

    /**
     * 执行交互式进程并获取信息
     * 
     * @param runProcess
     * @param input
     * @return
     * @throws InterruptedException
     */
    public static ExecuteResult getProcessMessage(Process runProcess, String input) {
        ExecuteResult executeResult = new ExecuteResult();

        try {
            StringReader inputReader = new StringReader(input);
            BufferedReader inputBufferedReader = new BufferedReader(inputReader);

            // 计时
            long start = System.currentTimeMillis();

            // 输入（模拟控制台输入）
            PrintWriter consoleInput = new PrintWriter(runProcess.getOutputStream());
            String line;
            while ((line = inputBufferedReader.readLine()) != null) {
                consoleInput.println(line);
            }
            consoleInput.flush();
            consoleInput.close();

            // 获取退出码
            int exitValue = runProcess.waitFor();

            executeResult.setExitCode(exitValue);
            executeResult.setStdout(getProcessOutput(runProcess.getInputStream()));
            executeResult.setStderr(getProcessOutput(runProcess.getErrorStream()));

            long end = System.currentTimeMillis();
            executeResult.setTime(end - start);
        } catch (Exception e) {
            log.error("运行失败：{}", e.toString());
        }
        return executeResult;
    }

    /**
     * 获取某个流的输出
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String getProcessOutput(InputStream inputStream) throws IOException {
        // 分批获取进程的正常输出
        // Linux写法
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        // Windows写法
        // BufferedReader bufferedReader = new BufferedReader(new
        // InputStreamReader(inputStream, "GBK"));
        StringBuilder outputSb = new StringBuilder();
        // 逐行读取
        String outputLine;
        while ((outputLine = bufferedReader.readLine()) != null) {
            outputSb.append(outputLine).append("\n");
        }
        bufferedReader.close();
        return outputSb.toString();
    }
}
