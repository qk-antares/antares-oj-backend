package com.antares.sandbox.template.java;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.antares.sandbox.model.dto.ExecuteResult;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.antares.sandbox.constant.SandBoxConstants.TIME_OUT;

public class JavaDockerAcmSandbox extends JavaSandboxTemplate{
    @Override
    protected List<ExecuteResult> runCode(String dir, List<String> inputList) throws IOException {
        return null;
    }
}
