package com.antares.codesandbox.template.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.antares.codesandbox.constant.SandboxConstants;
import com.antares.codesandbox.model.dto.ExecuteResult;
import com.antares.codesandbox.utils.ProcessUtils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JavaNativeAcmSandbox extends JavaSandboxTemplate {
    @Value("${antares.sandbox.security-manager-path:/root/workplace/java/antares-oj-backend/antares-code-sandbox/src/main/resources/security}")
    private String SECURITY_MANAGER_PATH;
    @Value("${antares.sandbox.security-manager-class-name:MySecurityManager}")
    private String SECURITY_MANAGER_CLASS_NAME;
    @Value("${antares.sandbox.java-home:/jdk-21.0.2/bin")
    private String JAVA_HOME;

    @Override
    protected List<ExecuteResult> runCode(String dir, List<String> inputList) throws IOException {
        // 3. 执行代码，得到输出结果
        List<ExecuteResult> executeResults = new ArrayList<>();
        for (String input : inputList) {
            // Linux下的命令
            String[] runCmd = { String.format("%s/java", "/jdk-21.0.2/bin"), "-Xmx256m", "-Dfile.encoding=UTF-8", "-cp",
                    String.format("%s:%s", dir,
                            "/root/workplace/java/antares-oj-backend/antares-code-sandbox/src/main/resources/security"),
                    String.format("-Djava.security.manager=%s", "MySecurityManager"), "Main" };
            // String[] runCmd = {String.format("%s/java", JAVA_HOME), "-Xmx256m",
            // "-Dfile.encoding=UTF-8", "-cp", String.format("%s:%s", dir,
            // SECURITY_MANAGER_PATH), String.format("-Djava.security.manager=%s",
            // SECURITY_MANAGER_CLASS_NAME), "Main"};
            // String runCmd = String.format("/software/jdk1.8.0_361/bin/java -Xmx256m
            // -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=%s Main", dir,
            // SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME);

            // Windows下的命令
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s
            // -Djava.security.manager=%s Main", dir, SECURITY_MANAGER_PATH,
            // SECURITY_MANAGER_CLASS_NAME);
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s
            // Main", dir);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(SandboxConstants.TIME_OUT);
                    // 超时了
                    runProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();

            ExecuteResult executeResult = null;
            try {
                executeResult = ProcessUtils.getAcmProcessMessage(runProcess, input);
            } catch (IOException e) {
                log.error("执行出错: {}", e.toString());
            }
            stopWatch.stop();
            if (!thread.isAlive()) {
                executeResult = new ExecuteResult();
                executeResult.setTime(stopWatch.getLastTaskTimeMillis());
                executeResult.setErrorOutput("超出时间限制");
            }
            executeResults.add(executeResult);

            // 已经有用例失败了
            if (StrUtil.isNotBlank(executeResult.getErrorOutput())) {
                break;
            }
        }
        return executeResults;
    }
}