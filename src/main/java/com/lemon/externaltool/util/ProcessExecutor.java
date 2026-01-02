package com.lemon.externaltool.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.lemon.externaltool.model.ExternalTool;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Process Executor
 * 执行外部进程的工具类
 */
public class ProcessExecutor {
    
    private static final Logger LOG = Logger.getInstance(ProcessExecutor.class);
    
    /**
     * 使用指定工具打开文件
     */
    public static void openWithTool(@NotNull ExternalTool tool, 
                                    @NotNull String filePath, 
                                    Project project) throws IOException {
        
        String executablePath = tool.getExecutablePath();
        File executable = new File(executablePath);
        
        // 验证可执行文件是否存在
        if (!executable.exists()) {
            throw new IOException("Executable not found: " + executablePath);
        }
        
        // 构建命令
        List<String> command = buildCommand(tool, filePath);
        
        LOG.info("Executing command: " + String.join(" ", command));
        
        try {
            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start();
            
            // 显示成功通知
            showSuccessNotification(project, tool.getName(), filePath);
            
        } catch (IOException e) {
            LOG.error("Failed to execute command", e);
            throw e;
        }
    }
    
    /**
     * 构建执行命令
     */
    private static List<String> buildCommand(@NotNull ExternalTool tool, @NotNull String filePath) {
        List<String> command = new ArrayList<>();
        String executablePath = tool.getExecutablePath();
        String commandTemplate = tool.getCommandTemplate();
        
        // 判断操作系统
        boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        
        // macOS应用需要使用open命令
        if (isMac && executablePath.endsWith(".app")) {
            command.add("open");
            command.add("-a");
            command.add(executablePath);
            command.add(filePath);
        } 
        // Windows和Linux直接执行
        else {
            if (commandTemplate != null && !commandTemplate.isEmpty()) {
                // 使用自定义命令模板
                String expandedCommand = expandCommandTemplate(commandTemplate, executablePath, filePath);
                // 简单的分词处理（处理引号）
                command.addAll(parseCommand(expandedCommand));
            } else {
                // 默认命令格式
                command.add(executablePath);
                command.add(filePath);
            }
        }
        
        return command;
    }
    
    /**
     * 扩展命令模板
     */
    private static String expandCommandTemplate(String template, String execPath, String filePath) {
        String result = template;
        
        // 替换占位符
        result = result.replace("{path}", execPath);
        result = result.replace("{file}", filePath);
        
        // 获取文件所在目录
        File file = new File(filePath);
        String fileDir = file.getParent();
        String fileName = file.getName();
        
        result = result.replace("{fileDir}", fileDir != null ? fileDir : "");
        result = result.replace("{fileName}", fileName);
        
        return result;
    }
    
    /**
     * 解析命令字符串（处理引号）
     */
    private static List<String> parseCommand(String command) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString());
        }
        
        return result;
    }
    
    /**
     * 显示成功通知
     */
    public static void showSuccessNotification(Project project, String toolName, String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        
        Notification notification = new Notification(
                "External Tool Opener",
                "File Opened",
                "Opened " + fileName + " with " + toolName,
                NotificationType.INFORMATION
        );
        
        Notifications.Bus.notify(notification, project);
    }
    
    /**
     * 显示错误通知
     */
    public static void showErrorNotification(Project project, String title, String message) {
        Notification notification = new Notification(
                "External Tool Opener",
                title,
                message,
                NotificationType.ERROR
        );
        
        Notifications.Bus.notify(notification, project);
    }
    
    /**
     * 测试工具是否可用
     */
    public static boolean testTool(@NotNull ExternalTool tool) {
        String executablePath = tool.getExecutablePath();
        File executable = new File(executablePath);
        
        // macOS应用包
        if (executablePath.endsWith(".app")) {
            return executable.exists() && executable.isDirectory();
        }
        
        // 普通可执行文件
        return executable.exists() && executable.canExecute();
    }
}
