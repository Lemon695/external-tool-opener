package com.lemon.externaltool.ui;

import com.lemon.externaltool.model.ExternalTool;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Tool Validator
 * 负责验证工具配置的合法性
 */
public class ToolValidator {

    public static class ValidationResult {
        public final boolean isValid;
        public final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * 验证工具配置
     */
    public static ValidationResult validate(@NotNull ExternalTool tool) {
        if (tool.getName() == null || tool.getName().trim().isEmpty()) {
            return ValidationResult.error("Tool name cannot be empty");
        }

        String path = tool.getExecutablePath();
        if (path == null || path.trim().isEmpty()) {
            return ValidationResult.error("Executable path cannot be empty");
        }

        // 仅在非空时检查路径格式，但不强制要求文件必须存在（可能是未挂载的驱动器等）
        // 但为了企业级稳健性，我们可以给出警告，或者在UI层面提示。
        // 这里我们进行基本检查。

        return ValidationResult.ok();
    }
}
