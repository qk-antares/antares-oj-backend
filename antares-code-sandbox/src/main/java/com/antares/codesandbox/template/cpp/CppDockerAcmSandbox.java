package com.antares.codesandbox.template.cpp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.template.SandboxTemplate;

@Component("cppAcmSandbox")
// 只在开启docker代码沙箱时加载
@ConditionalOnProperty(name = "antares.code-sandbox.type", havingValue = "docker")
public class CppDockerAcmSandbox extends SandboxTemplate {

    @Override
    protected ExecuteCodeRes compileAndRun(File codeFile, List<String> inputList) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'compileAndRun'");
    }
    
}
