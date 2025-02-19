package com.antares.codesandbox.constant;

import java.util.List;

public interface SandboxConstants {
    String FILE_NAME = "Main";
    long TIME_OUT = 5 * 1000L;
    List<String> BANNED_WORDS = List.of("Files", "exec");
    String JDK_IMAGE = "openjdk:8-alpine";
}
