package com.antares.codesandbox.template;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.antares.codesandbox.model.dto.ExecuteCodeRes;
import com.antares.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;

import cn.hutool.core.io.FileUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RefreshScope
public abstract class SandboxTemplate {
    private static final WordTree WORD_TREE;
    @Value("${antares.sandbox.java-xmx:128}")
    protected int javaXmx;
    @Value("${antares.sandbox.timeout:1000}")
    protected long timeout;
    @Value("${antares.sandbox.filename:Main}")
    protected String filename;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords("Files", "exec");
    }

    /**
     * 模板方法，定义运行步骤
     */
    public final ExecuteCodeRes executeCode(List<String> inputList, String code, String ext) {
        // 1. 把用户的代码保存为文件
        String dir = null;
        File codeFile = null;
        dir = String.join(File.separator, System.getProperty("user.dir"), "tmpCode", UUID.randomUUID().toString());
        try {
            codeFile = saveFile(code, dir, ext);
        } catch (BusinessException e) {
            return ExecuteCodeRes.builder()
                    .code(ExecuteCodeStatusEnum.COMPILE_FAILED.getValue())
                    .msg(e.getMessage())
                    .build();
        }

        // 2. 编译运行代码
        ExecuteCodeRes response = null;
        try {
            response = compileAndRun(codeFile, inputList);
        } catch (IOException | InterruptedException e) {
            log.error("代码编译运行失败，服务器内部错误：", e);
            return ExecuteCodeRes.builder()
                    .code(ExecuteCodeStatusEnum.RUN_FAILED.getValue())
                    .msg("服务器内部错误")
                    .build();
        } finally {
            // 3. 清理临时文件
            clearFile(codeFile, dir);
        }

        return response;
    }

    /**
     * 将code保存在dir目录下
     * 
     * @param code
     * @param dir
     * @return
     */
    protected File saveFile(String code, String dir, String ext) {
        // 检查代码内容，是否有黑名单代码
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            throw new BusinessException(HttpCodeEnum.BAD_REQUEST, "危险代码");
        }

        String path = dir + File.separator + this.filename + ext;
        log.info("代码保存路径：{}", path);
        File codFile = FileUtil.writeUtf8String(code, path);

        try {
            // 设置权限为777
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.addAll(Arrays.asList(PosixFilePermission.values()));
            Files.setPosixFilePermissions(Paths.get(dir), permissions);
        } catch (IOException e) {
            throw new BusinessException(HttpCodeEnum.INTERNAL_SERVER_ERROR, "文件权限设置失败");
        }

        return codFile;
    }

    /**
     * 编译并运行codeFile，NativeAcm和DockerAcm有不同实现
     * 
     * @param codeFile
     * @return
     * @throws IOException
     */
    protected abstract ExecuteCodeRes compileAndRun(File codeFile, List<String> inputList)
            throws IOException, InterruptedException;

    /**
     * 文件清理
     * 
     * @param codeFile
     * @param dir
     */
    protected void clearFile(File codeFile, String dir) {
        if (codeFile.getParentFile() != null) {
            boolean del = FileUtil.del(dir);
            log.info("删除{}: {}", del ? "成功" : "失败", dir);
        }
    }
}
