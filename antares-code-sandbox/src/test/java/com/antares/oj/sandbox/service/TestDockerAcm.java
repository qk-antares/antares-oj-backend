package com.antares.oj.sandbox.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;

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
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestDockerAcm {
    public static void main(String[] args) throws IOException, InterruptedException {
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("tcp://172.17.0.1:2375")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        // 镜像已经提前下载
        String image = "openjdk:8-alpine";

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
        hostConfig.withMemory(128 * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        // CPU限制为单核
        hostConfig.withCpuCount(1L);
        // 绑定代码目录，代码会通过容器外部的Java程序写入该目录，容器只负责编译和执行
        hostConfig.setBinds(new Bind("/docker/code/java/antares-oj-backend/tmpCode", new Volume("/app")));

        CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(image)
                .withName("jdk")
                .withHostConfig(hostConfig)
                .withUser("nobody")
                .withTty(true)
                .exec();

        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();

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

        // =============================================================
        // Step 4: 准备监控内存占用
        final long[] maxMemoryUsage = { 0L };

        // 运行预先准备好的内存监控脚本
        ExecCreateCmdResponse memResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "/app/mem.sh", "0.01")
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

        // StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        // ResultCallback<Statistics> statsCallback = statsCmd.exec(new ResultCallback.Adapter<Statistics>() {
        //     @Override
        //     public void onNext(Statistics stats) {
        //         long memoryUsage = stats.getMemoryStats().getUsage();

        //         // 记录最大内存使用量
        //         maxMemoryUsage[0] = Math.max(maxMemoryUsage[0], memoryUsage);
        //         countDownLatch.countDown();
        //         System.out.println("Current memory usage: " + memoryUsage);
        //     }
        // });

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
        long startTime = System.nanoTime();

        // 使用 ResultCallback 捕获输出流和错误流
        dockerClient.execStartCmd(runExecId).withStdIn(containerInput).exec(new ResultCallback.Adapter<Frame>() {
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
        }).awaitCompletion();

        // 停止计时
        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime) / 1_000_000; // 转换为毫秒

        // 获取输出结果
        output = runStdout.toString("UTF-8");
        errorOutput = runStderr.toString("UTF-8");

        // 关闭统计
        // Thread.sleep(500);
        memInputPipe.write("q".getBytes());
        memInputPipe.flush();
        memInputPipe.close();

        System.out.println("Standard Output: " + output);
        System.out.println("Error Output: " + errorOutput);
        System.out.println("memStdout: " + memStdout.toString("UTF-8"));
        System.out.println("memStderr: " + memStderr.toString("UTF-8"));
        System.out.println("Elapsed Time: " + elapsedTime + " ms");
        System.out.println("Max Memory Usage: " + maxMemoryUsage[0] + " bytes");

        // 停止并移除容器
        // dockerClient.stopContainerCmd(containerId).exec();
        // dockerClient.removeContainerCmd(containerId).exec();

        // httpClient.close();
        // dockerClient.close();

        // 输入列表
        // List<String> inputList = Arrays.asList("1 3", "2 4");

        // for (String input : inputList) {
        // // 准备传递到容器中的输入流
        // try (PipedOutputStream pipedOut = new PipedOutputStream();
        // PipedInputStream pipedIn = new PipedInputStream(pipedOut)) {

        // // 创建 Exec 命令来运行 Java 程序
        // ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
        // .withCmd("java", "-cp", "/app/output", "Main")
        // .withAttachStdin(true)
        // .withAttachStdout(true)
        // .withAttachStderr(true)
        // .exec();

        // // 启动 Exec 并将输入写入 stdin
        // dockerClient.execStartCmd(execResponse.getId())
        // .withStdIn(pipedIn)
        // .exec(new ExecStartResultCallback(System.out, System.err));

        // // 将输入写入到 pipedOut，这样 Java 程序的 Scanner 就能读取到
        // pipedOut.write((input + "\n").getBytes());
        // pipedOut.flush();
        // }
        // }

        // // 停止并移除容器
        // dockerClient.stopContainerCmd(container.getId()).exec();
        // dockerClient.removeContainerCmd(container.getId()).exec();

        // // 创建容器
        // CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);

        // CreateContainerResponse createContainerResponse = containerCmd
        // .withHostConfig(hostConfig)
        // .withNetworkDisabled(true)
        // .withAttachStdin(true)
        // .withAttachStderr(true)
        // .withAttachStdout(true)
        // .withTty(true) // 交互式，会开启/bin/bash
        // .exec();

        // // 启动容器
        // String containerId = createContainerResponse.getId();
        // dockerClient.startContainerCmd(containerId).exec();

        // // 输入参数
        // List<ExecuteResult> executeResults = new ArrayList<>();
        // for (String inputArgs : inputList) {
        // String[] inputArgsArray = inputArgs.split(" ");
        // String[] cmdArray = ArrayUtil.append(new String[] { "java", "-cp", "/app",
        // "Main" }, inputArgsArray);
        // // 创建执行命令
        // ExecCreateCmdResponse execCreateCmdResponse =
        // dockerClient.execCreateCmd(containerId)
        // .withCmd(cmdArray)
        // .withAttachStderr(true)
        // .withAttachStdin(true)
        // .withAttachStdout(true)
        // .exec();

        // String execId = execCreateCmdResponse.getId();

        // ExecuteResult executeResult = new ExecuteResult();
        // // 执行时间
        // long time = 0L;
        // // 判断是否超时
        // final boolean[] timeout = { true };
        // // 正常输出
        // final String[] message = { null };
        // // 错误输出
        // final String[] errorMessage = { null };

        // ResultCallback.Adapter<Frame> callBack = new ResultCallback.Adapter<Frame>()
        // {
        // @Override
        // public void onComplete() {
        // // 如果执行完成，则表示没超时
        // timeout[0] = false;
        // super.onComplete();
        // }

        // @Override
        // public void onNext(Frame frame) {
        // StreamType streamType = frame.getStreamType();
        // if (StreamType.STDERR.equals(streamType)) {
        // errorMessage[0] = new String(frame.getPayload());
        // System.out.println("输出错误结果：" + errorMessage[0]);
        // } else {
        // message[0] = new String(frame.getPayload());
        // System.out.println("输出结果：" + message[0]);
        // }
        // super.onNext(frame);
        // }
        // };

        // final long[] maxMemory = { 0L };

        // // 获取占用的内存
        // StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        // ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new
        // ResultCallback<Statistics>() {
        // @Override
        // public void onNext(Statistics statistics) {
        // System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
        // maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(),
        // maxMemory[0]);
        // }

        // @Override
        // public void close() throws IOException {

        // }

        // @Override
        // public void onStart(Closeable closeable) {

        // }

        // @Override
        // public void onError(Throwable throwable) {

        // }

        // @Override
        // public void onComplete() {

        // }
        // });
        // statsCmd.exec(statisticsResultCallback);

        // StopWatch stopWatch = new StopWatch();
        // try {
        // stopWatch.start();
        // dockerClient.execStartCmd(execId)
        // .exec(callBack)
        // .awaitCompletion(SandboxConstants.TIME_OUT, TimeUnit.MILLISECONDS);
        // stopWatch.stop();
        // time = stopWatch.getLastTaskTimeMillis();
        // statsCmd.close();
        // } catch (InterruptedException e) {
        // System.out.println("程序执行异常");
        // throw new RuntimeException(e);
        // }
        // executeResult.setOutput(message[0]);
        // executeResult.setErrorOutput(errorMessage[0]);
        // executeResult.setTime(time);
        // executeResult.setMemory(maxMemory[0]);
        // executeResults.add(executeResult);
        // }

        // System.out.println(executeResults);

        // StopContainerCmd stopContainerCmd =
        // dockerClient.stopContainerCmd(containerId);
        // stopContainerCmd.exec();
        // RemoveContainerCmd removeContainerCmd =
        // dockerClient.removeContainerCmd(containerId);
        // removeContainerCmd.exec();
    }
}
