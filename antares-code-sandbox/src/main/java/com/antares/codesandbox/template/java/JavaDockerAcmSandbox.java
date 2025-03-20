package com.antares.codesandbox.template.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.model.dto.ExecuteResult;
import com.antares.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.antares.codesandbox.model.enums.ExitCodeEnum;
import com.antares.codesandbox.template.SandboxTemplate;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Component("javaAcmSandbox")
// 只在开启docker代码沙箱时加载
@ConditionalOnProperty(name = "antares.code-sandbox.type", havingValue = "docker")
@RefreshScope
@Slf4j
public class JavaDockerAcmSandbox extends SandboxTemplate {
    @Value("${antares.code-sandbox.memory-limit:128}")
    private long memoryLimit;
    @Value("${antares.code-sandbox.cpu-count:1}")
    private long cpuCount;
    @Value("${antares.code-sandbox.save-path:/docker/code/java/antares-oj-backend/tmpCode}")
    private String savePath;
    @Value("${antares.code-sandbox.mem-script:/docker/code/java/antares-oj-backend/script/mem.sh}")
    private String memScript;
    @Value("${antares.code-sandbox.jdk-image:openjdk:8-alpine}")
    private String jdkImage;

    @Resource
    private DockerClient dockerClient;

    @Override
    protected ExecuteCodeRes compileAndRun(File codeFile, List<String> inputList)
            throws IOException, InterruptedException {
        // 1. 创建容器
        String codeId = codeFile.getParentFile().getName();
        String containerId = createContainer(codeId);

        try {
            String stdout;
            String stderr;

            // 2. 编译 Main.java
            String compileExecId = dockerClient.execCreateCmd(containerId)
                    .withCmd("javac", "-d", "/app", "/app/Main.java")
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec()
                    .getId();

            // 创建输出流来接收编译结果
            ByteArrayOutputStream compileStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream compileStderr = new ByteArrayOutputStream();

            // 执行命令并使用 ResultCallback 捕获输出
            dockerClient.execStartCmd(compileExecId).exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    try {
                        if (frame.getStreamType() == StreamType.STDOUT) {
                            compileStdout.write(frame.getPayload());
                        } else if (frame.getStreamType() == StreamType.STDERR) {
                            compileStderr.write(frame.getPayload());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).awaitCompletion();

            // 获取编译的输出结果
            stdout = compileStdout.toString("UTF-8").trim();
            stderr = compileStderr.toString("UTF-8").trim();
            log.info("编译标准输出: {}", stdout);
            log.info("编译错误输出: {}", stderr);
            if (StrUtil.isNotBlank(stderr)) {
                return ExecuteCodeRes.builder()
                        .code(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())
                        .msg(stderr)
                        .build();
            }

            // 3. 运行编译后的 Java 文件，并传入输入数据
            List<ExecuteResult> executeResults = new ArrayList<>();

            if (CollectionUtil.isEmpty(inputList)) {
                inputList = new ArrayList<>();
                inputList.add(null);
            }

            for (String input : inputList) {
                // 4: 开始监控内存占用
                AtomicLong maxMemoryUsage = new AtomicLong(0L);
                // 运行预先准备好的内存监控脚本
                ExecCreateCmdResponse memResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd("/bin/sh", "/script/mem.sh", "0.01")
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .exec();

                String memExecId = memResponse.getId();

                // 创建输入流，当接收到q输入时退出内存监控
                PipedOutputStream memInputPipe = new PipedOutputStream();
                PipedInputStream memInput = new PipedInputStream(memInputPipe);
                ByteArrayOutputStream memStdout = new ByteArrayOutputStream();
                ByteArrayOutputStream memStderr = new ByteArrayOutputStream();

                // 使用 ResultCallback 捕获输出流和错误流
                dockerClient.execStartCmd(memExecId).withStdIn(memInput).exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        try {
                            if (frame.getStreamType() == StreamType.STDOUT) {
                                memStdout.write(frame.getPayload());
                                maxMemoryUsage
                                        .set(Math.max(maxMemoryUsage.get(),
                                                Long.valueOf(new String(frame.getPayload()).trim())));
                            } else if (frame.getStreamType() == StreamType.STDERR) {
                                memStderr.write(frame.getPayload());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                ExecCreateCmdResponse runResponse = dockerClient.execCreateCmd(containerId)
                        .withCmd("java", String.format("-Xmx%dm", this.javaXmx), "-cp", "/app", "Main")
                        .withAttachStdin(true)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .exec();

                String runExecId = runResponse.getId();

                // 创建输入流，将数据传递给 Java 程序
                PipedOutputStream runInputPipe = new PipedOutputStream();
                PipedInputStream runInput = new PipedInputStream(runInputPipe);
                ByteArrayOutputStream runStdout = new ByteArrayOutputStream();
                ByteArrayOutputStream runStderr = new ByteArrayOutputStream();

                // 向程序传递输入数据
                if (input != null) {
                    runInputPipe.write(input.getBytes());
                    runInputPipe.write("\n".getBytes());
                    runInputPipe.flush();
                    runInputPipe.close();
                }

                // 开始计时
                long startTime = System.currentTimeMillis();

                // 使用 ResultCallback 捕获输出流和错误流
                ResultCallback.Adapter<Frame> callback = dockerClient.execStartCmd(runExecId).withStdIn(runInput)
                        .exec(new ResultCallback.Adapter<Frame>() {
                            @Override
                            public void onNext(Frame frame) {
                                try {
                                    if (frame.getStreamType() == StreamType.STDOUT) {
                                        runStdout.write(frame.getPayload());
                                    } else if (frame.getStreamType() == StreamType.STDERR) {
                                        runStderr.write(frame.getPayload());
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                // 超时控制
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(this.timeout);
                        // 超时了，直接停止callback
                        callback.close();
                    } catch (InterruptedException e) {
                        log.info("超时控制线程终止，代码在时间限制内运行完成");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();

                callback.awaitCompletion();

                // 停止计时
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                // 关闭统计
                memInputPipe.write("q".getBytes());
                memInputPipe.flush();
                memInputPipe.close();

                // 获取运行输出结果
                stdout = runStdout.toString("UTF-8").trim();
                stderr = runStderr.toString("UTF-8").trim();
                log.info("运行标准输出: {}", stdout);
                log.info("运行错误输出: {}", stderr);
                log.info("内存标准输出: " + memStdout.toString("UTF-8").trim().replace('\n', ' '));
                log.info("内存错误输出: " + memStderr.toString("UTF-8").trim().replace('\n', ' '));
                log.info("耗时: {} ms", elapsedTime);
                log.info("内存占用: {} bytes", maxMemoryUsage.get());

                ExecuteResult executeResult = ExecuteResult.builder()
                        .exitCode(0)
                        .stdout(stdout)
                        .stderr(stderr)
                        .time(elapsedTime)
                        .memory(maxMemoryUsage.get())
                        .build();

                if (!thread.isAlive()) {
                    executeResult.setExitCode(-1);
                    executeResult.setStderr("超出时间限制");
                }

                thread.interrupt();

                if (StrUtil.isNotBlank(stderr)) {
                    executeResult.setExitCode(-1);
                }

                executeResults.add(executeResult);

                // 已经有用例失败了
                if (executeResult.getExitCode() != ExitCodeEnum.SUCCESS.getValue()) {
                    ExecuteCodeStatusEnum statusEnum = ExecuteCodeStatusEnum
                            .getEnumByExitCodeEnum(ExitCodeEnum.getEnumByValue(executeResult.getExitCode()));
                    return ExecuteCodeRes.builder()
                            .code(statusEnum.getValue())
                            .msg(statusEnum.getMsg())
                            .build();
                }
            }

            // 返回结果
            return ExecuteCodeRes.builder()
                    .code(ExecuteCodeStatusEnum.SUCCESS.getValue())
                    .msg(ExecuteCodeStatusEnum.SUCCESS.getMsg())
                    .results(executeResults)
                    .build();
        } finally {
            // 5. 停止并移除容器
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    dockerClient.stopContainerCmd(containerId).exec();
                    dockerClient.removeContainerCmd(containerId).exec();
                }
            });
        }
    }

    public String createContainer(String codeId) {
        // 1. 创建并启动容器
        // 容器配置
        HostConfig hostConfig = new HostConfig();
        // 禁止文件写入
        hostConfig.withReadonlyRootfs(true);
        // 禁止网络访问
        hostConfig.withNetworkMode("none");
        // 限制进程数，防止滥用
        hostConfig.withPidsLimit(64L);
        // 限制权限
        hostConfig.withCapDrop(Capability.ALL);
        // 内存限制为128MB，交换区0MB
        hostConfig.withMemory(memoryLimit * 1024 * 1024);
        hostConfig.withMemorySwap(0L);
        // CPU限制为单核
        hostConfig.withCpuCount(cpuCount);
        // 绑定代码目录，代码会通过容器外部的Java程序写入该目录，容器只负责编译和执行
        hostConfig.setBinds(new Bind(savePath + File.separator + codeId, new Volume("/app")),
                new Bind(memScript, new Volume("/script/mem.sh")));

        CreateContainerResponse createContainerResponse = dockerClient
                .createContainerCmd(this.jdkImage)
                .withName(codeId)
                .withHostConfig(hostConfig)
                .withUser("nobody")
                .withTty(true)
                .exec();

        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();

        return containerId;
    }
}
