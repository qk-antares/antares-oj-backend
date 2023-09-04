package com.antares.sandbox.service;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.antares.sandbox.model.dto.ExecuteResult;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.springframework.util.StopWatch;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestDockerAcm {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> inputList = new ArrayList<>(Arrays.asList("1 3", "2 4"));

        DefaultDockerClientConfig dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("tcp://debian.antares.cool:2375")
                .build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).build();

        //镜像已经提前下载
        String image = "openjdk:8-alpine";

        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        // 容器配置
        HostConfig hostConfig = new HostConfig();

        String seccomp = ResourceUtil.readStr("seccomp.json", StandardCharsets.UTF_8);
        hostConfig.withSecurityOpts(Collections.singletonList("seccomp=" + seccomp));
        hostConfig.withMemory(128 * 1024 * 1024L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.setBinds(new Bind("/www/wwwroot/oj.antares.cool-backend/tmpCode", new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)  //交互式，会开启/bin/bash
                .exec();

        //启动容器
        String containerId = createContainerResponse.getId();
        dockerClient.startContainerCmd(containerId).exec();

        //输入参数
        List<ExecuteResult> executeResults = new ArrayList<>();
        for (String inputArgs : inputList) {
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"});
            //创建执行命令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();

            String execId = execCreateCmdResponse.getId();

            //执行时间
            long time = 0L;
            // 判断是否超时
            final boolean[] timeout = {true};
            // 正常输出
            final String[] message = {null};
            // 错误输出
            final String[] errorMessage = {null};

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };

            dockerClient.execStartCmd(execId)
                    .withStdIn(new ByteArrayInputStream("1 3\nhello".getBytes()))
                    .exec(execStartResultCallback)
                    .awaitCompletion();
        }

        System.out.println(executeResults);

        StopContainerCmd stopContainerCmd = dockerClient.stopContainerCmd(containerId);
        stopContainerCmd.exec();
        RemoveContainerCmd removeContainerCmd = dockerClient.removeContainerCmd(containerId);
        removeContainerCmd.exec();
    }
}
