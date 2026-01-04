package com.lemon.externaltool.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.service.ToolExecutionService;
import org.jetbrains.annotations.NotNull;

/**
 * Open With Sub Action
 * 单个工具的菜单项动作
 */
public class OpenWithSubAction extends AnAction {

    private final ExternalTool tool;

    public OpenWithSubAction(@NotNull ExternalTool tool) {
        super(tool.getName());
        this.tool = tool;

        // 如果是默认工具，添加标记
        if (tool.isDefault()) {
            getTemplatePresentation().setText(tool.getName() + " (default)");
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project != null && file != null) {
            // Use the new Execution Service
            ToolExecutionService.getInstance(project).execute(tool, file.getPath());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always enable/visible if created. Logic was handled in Group.
        e.getPresentation().setEnabledAndVisible(true);
    }
}
