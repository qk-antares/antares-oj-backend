package com.antares.sandbox.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWrite {
    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("/www/wwwroot/oj.antares.cool-backend/tmpCode/123"));
    }
}
