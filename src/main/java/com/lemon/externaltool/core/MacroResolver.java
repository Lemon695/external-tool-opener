package com.lemon.externaltool.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Macro Resolver
 * 负责解析和替换命令中的变量宏
 */
public class MacroResolver {

    // 预定义的宏变量名
    public static final String MACRO_PATH = "{path}";
    public static final String MACRO_FILE = "{file}";
    public static final String MACRO_FILE_DIR = "{fileDir}";
    public static final String MACRO_FILE_NAME = "{fileName}";

    /**
     * 解析命令模板
     *
     * @param template       命令模板
     * @param execPath       执行文件路径
     * @param targetFilePath 目标文件路径
     * @return 解析后的命令字符串
     */
    public String resolve(String template, String execPath, String targetFilePath) {
        if (template == null)
            return "";

        Map<String, String> macros = new HashMap<>();
        macros.put(MACRO_PATH, execPath);
        macros.put(MACRO_FILE, targetFilePath);

        if (targetFilePath != null) {
            File file = new File(targetFilePath);
            macros.put(MACRO_FILE_DIR, file.getParent() != null ? file.getParent() : "");
            macros.put(MACRO_FILE_NAME, file.getName());
        } else {
            macros.put(MACRO_FILE_DIR, "");
            macros.put(MACRO_FILE_NAME, "");
        }

        String result = template;
        for (Map.Entry<String, String> entry : macros.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
