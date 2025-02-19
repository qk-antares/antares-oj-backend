package com.antares.codesandbox.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.antares.codesandbox.constant.SandboxConstants;
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

@SpringBootTest
public class TestJavaDockerAcmSandbox {
    @Value("${antares.code-sandbox.memory-limit:128}")
    private long memoryLimit;
    @Value("${antares.code-sandbox.cpu-count:1}")
    private long cpuCount;
    @Value("${antares.code-sandbox.save-path:/docker/code/java/antares-oj-backend/tmpCode}")
    private String savePath;
    @Value("${antares.code-sandbox.mem-script:/docker/code/java/antares-oj-backend/script/mem.sh}")
    private String memScript;

    @Resource
    private DockerClient dockerClient;

    @Test
    public void functionTest() throws IOException, InterruptedException {
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
        hostConfig.setBinds(new Bind(savePath, new Volume("/app")),
                new Bind(memScript, new Volume("/script/mem.sh")));

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(SandboxConstants.JDK_IMAGE)
                .withName(UUID.randomUUID().toString())
                .withHostConfig(hostConfig)
                .withUser("nobody")
                .withTty(true)
                .exec();

        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();

        try {
            // 编译 Main.java
            ExecCreateCmdResponse compileResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("javac", "-d", "/app", "/app/Main.java")
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();

            String compileExecId = compileResponse.getId();

            // 创建输出流来接收编译结果
            ByteArrayOutputStream compileStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream compileStderr = new ByteArrayOutputStream();

            // 使用 ResultCallback 捕获输出
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
            String output = compileStdout.toString("UTF-8");
            String errorOutput = compileStderr.toString("UTF-8");
            System.out.println("Standard Output: " + output);
            System.out.println("Error Output: " + errorOutput);

            // Step 4: 准备监控内存占用
            final long[] maxMemoryUsage = { 0L };

            // 运行预先准备好的内存监控脚本
            ExecCreateCmdResponse memResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", "/script/mem.sh", "0.01")
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String memExecId = memResponse.getId();

            // Step 3: 创建输入流，输入q退出内存监控
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
                        } else if (frame.getStreamType() == StreamType.STDERR) {
                            memStderr.write(frame.getPayload());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onComplete() {
                    System.out.println("Memory monitoring stopped.");
                }
            });

            // 运行编译后的 Java 文件，并传入输入数据
            ExecCreateCmdResponse runResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("java", "-cp", "/app", "Main")
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            String runExecId = runResponse.getId();

            // Step 3: 创建输入流，将数据传递给 Java 程序
            PipedOutputStream inputPipe = new PipedOutputStream();
            PipedInputStream containerInput = new PipedInputStream(inputPipe);
            ByteArrayOutputStream runStdout = new ByteArrayOutputStream();
            ByteArrayOutputStream runStderr = new ByteArrayOutputStream();

            // Step 4: 向程序传递输入数据
            String input = "2 3\n"; // 输入数据，供 Java 程序读取
            inputPipe.write(input.getBytes());
            inputPipe.flush();
            inputPipe.close();

            // 开始计时
            long startTime = System.currentTimeMillis();

            // 使用 ResultCallback 捕获输出流和错误流
            ResultCallback.Adapter<Frame> callback = dockerClient.execStartCmd(runExecId).withStdIn(containerInput)
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
                    Thread.sleep(SandboxConstants.TIME_OUT);
                    // 超时了，直接停止callback
                    callback.close();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

            callback.awaitCompletion();

            if (!thread.isAlive()) {
                System.out.println("超出时间限制");
                return;
            }

            // 停止计时
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            // 获取输出结果
            output = runStdout.toString("UTF-8");
            errorOutput = runStderr.toString("UTF-8");

            // 关闭统计
            memInputPipe.write("q".getBytes());
            memInputPipe.flush();
            memInputPipe.close();

            System.out.println("Standard Output: " + output);
            System.out.println("Error Output: " + errorOutput);
            System.out.println("memStdout: " + memStdout.toString("UTF-8"));
            System.out.println("memStderr: " + memStderr.toString("UTF-8"));
            System.out.println("Elapsed Time: " + elapsedTime + " ms");
            System.out.println("Max Memory Usage: " + maxMemoryUsage[0] + " bytes");
        } finally {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
        }
    }

    @Test
    public void timeoutTest() throws IOException, InterruptedException {
        Thread.sleep(5 * 60 * 1000);

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
        hostConfig.setBinds(new Bind(savePath, new Volume("/app")),
                new Bind(memScript, new Volume("/script/mem.sh")));

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(SandboxConstants.JDK_IMAGE)
                .withName(UUID.randomUUID().toString())
                .withHostConfig(hostConfig)
                .withUser("nobody")
                .withTty(true)
                .exec();

        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();
    }

    @Test
    public void parallelTest() throws IOException, InterruptedException {
        final int totalThreads = 10;
        final long delay = 50; // 毫秒
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        for (int i = 0; i < totalThreads; i++) {
            scheduler.schedule(() -> {
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
                hostConfig.setBinds(new Bind(savePath, new Volume("/app")),
                        new Bind(memScript, new Volume("/script/mem.sh")));

                CreateContainerResponse createContainerResponse = dockerClient
                        .createContainerCmd(SandboxConstants.JDK_IMAGE)
                        .withName(UUID.randomUUID().toString())
                        .withHostConfig(hostConfig)
                        .withUser("nobody")
                        .withTty(true)
                        .exec();

                String containerId = createContainerResponse.getId();
                dockerClient.startContainerCmd(containerId).exec();
            }, delay * i, TimeUnit.MILLISECONDS);
        }

        // 关闭调度器，等待任务完成后关闭
        scheduler.shutdown();
    }
}
