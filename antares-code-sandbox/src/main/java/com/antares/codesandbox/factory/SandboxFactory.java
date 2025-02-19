package com.antares.codesandbox.factory;

import com.antares.codesandbox.template.SandboxTemplate;

public interface SandboxFactory {
    SandboxTemplate createSandboxTemplate(String type);
}
