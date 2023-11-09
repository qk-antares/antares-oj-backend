package com.antares.sandbox.template.java;

import com.antares.sandbox.model.dto.ExecuteResult;
import com.antares.sandbox.utils.ProcessUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.antares.sandbox.constant.SandBoxConstants.TIME_OUT;

@Service
public class JavaNativeArgsSandbox extends JavaSandboxTemplate {
    @Override
    protected List<ExecuteResult> runCode(String dir, List<String> inputList) throws IOException {
        List<ExecuteResult> messages = new ArrayList<>();
        for (String input : inputList) {
            //Linux下的命令
            String runCmd = String.format("/software/jdk1.8.0_361/bin/java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", dir, input);
            //Windows下命令
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", dir, input);
            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制
            new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    System.out.println("超时了，中断");
                    runProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            ExecuteResult executeResult = ProcessUtils.getProcessMessage(runProcess, "运行");
            messages.add(executeResult);
        }
        return messages;
    }
}
