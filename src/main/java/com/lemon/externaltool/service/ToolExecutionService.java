package com.lemon.externaltool.service;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.lemon.externaltool.core.MacroResolver;
import com.lemon.externaltool.model.ExternalTool;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool Execution Service
 * 负责执行外部工具的业务逻辑
 */
@Service(Service.Level.PROJECT)
public final class ToolExecutionService {

    private static final Logger LOG = Logger.getInstance(ToolExecutionService.class);
    private final Project project;
    private final MacroResolver macroResolver;

    public ToolExecutionService(Project project) {
        this.project = project;
        this.macroResolver = new MacroResolver();
    }

    public static ToolExecutionService getInstance(Project project) {
        return project.getService(ToolExecutionService.class);
    }

    /**
     * 执行工具打开文件
     */
    public void execute(@NotNull ExternalTool tool, @NotNull String filePath) {
        try {
            validate(tool, filePath);
            List<String> command = buildCommand(tool, filePath);
            runProcess(command);
            notifySuccess(tool, filePath);
        } catch (Exception e) {
            LOG.error("Execution failed for tool: " + tool.getName(), e);
            notifyError(tool, e.getMessage());
        }
    }

    private void validate(ExternalTool tool, String filePath) throws IOException {
        String execPath = tool.getExecutablePath();
        if (execPath == null || execPath.isEmpty()) {
            throw new IOException("Tool path is empty");
        }

        File execFile = new File(execPath);
        boolean isMacAndApp = isMacAppBundle(execPath);

        // macOS .app is a directory, verify it exists
        if (isMacAndApp) {
            if (!execFile.exists() || !execFile.isDirectory()) {
                throw new IOException("Application bundle not found: " + execPath);
            }
        } else {
            // Normal executable must be a file
            if (!execFile.exists() || !execFile.isFile()) {
                throw new IOException("Executable file not found: " + execPath);
            }
            if (!execFile.canExecute()) {
                // Try to warn, but sometimes canExecute returns false for valid scripts
                // depending on ACLs
                LOG.warn("File might not be executable: " + execPath);
            }
        }
    }

    private List<String> buildCommand(ExternalTool tool, String filePath) {
        List<String> command = new ArrayList<>();
        String execPath = tool.getExecutablePath();
        String temp = tool.getCommandTemplate();

        if (isMacAppBundle(execPath)) {
            // macOS .app Strategy: use 'open -a'
            // We ignore complex templates for .app bundles to ensure reliability
            command.add("open");
            command.add("-a");
            command.add(execPath);
            command.add(filePath);

            // Note: Custom arguments for .app are tricky with 'open'.
            // We prioritize simply opening the file which covers 99% of use cases (VSCode,
            // Typora, Trae).
        } else {
            // CLI Strategy: Standard Execution
            if (temp != null && !temp.isEmpty()) {
                // Use Macro Resolver
                String expanded = macroResolver.resolve(temp, execPath, filePath);
                command.addAll(parseCommandArgs(expanded));
            } else {
                // Default fallback
                command.add(execPath);
                command.add(filePath);
            }
        }
        return command;
    }

    private boolean isMacAppBundle(String path) {
        return path != null && path.endsWith(".app") && System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * 简单的参数解析，支持引号
     */
    private List<String> parseCommandArgs(String cmdParams) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < cmdParams.length(); i++) {
            char c = cmdParams.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            args.add(current.toString());
        }
        return args;
    }

    private void runProcess(List<String> command) throws IOException {
        LOG.info("Running command: " + command);
        ProcessBuilder pb = new ProcessBuilder(command);
        // Ensure child process isn't killed when IDE closes?
        // Not strictly necessary for 'open' but good practice.
        // Redirect output to avoid buffer blocking
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Non-blocking drain of output stream to prevent deadlocks
        new Thread(() -> {
            try (java.io.InputStream stream = process.getInputStream()) {
                while (stream.read() != -1) {
                    // discard output
                }
            } catch (IOException ignored) {
            }
        }).start();
    }

    private void notifySuccess(ExternalTool tool, String filePath) {
        File f = new File(filePath);
        String title = "Tool Launched";
        String content = "Opened " + f.getName() + " in " + tool.getName();

        Notifications.Bus.notify(new Notification(
                "External Tool Opener",
                title,
                content,
                NotificationType.INFORMATION), project);
    }

    private void notifyError(ExternalTool tool, String message) {
        Notifications.Bus.notify(new Notification(
                "External Tool Opener",
                "Execution Failed",
                "Failed to run " + tool.getName() + ": " + message,
                NotificationType.ERROR), project);
    }
}
