package com.lemon.externaltool.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.service.ExternalToolService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Open With Action Group
 * "Open With..." 右键菜单动作组
 */
public class OpenWithActionGroup extends ActionGroup {
    
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        if (e == null) {
            return AnAction.EMPTY_ARRAY;
        }
        
        Project project = e.getProject();
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }
        
        // 获取当前选中的文件
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null || file.isDirectory()) {
            return AnAction.EMPTY_ARRAY;
        }
        
        // 获取ExternalToolService
        ExternalToolService service = ExternalToolService.getInstance(project);
        
        // 获取适用于该文件的工具列表
        List<ExternalTool> tools = service.getToolsForFile(file);
        
        if (tools.isEmpty()) {
            // 如果没有可用工具，显示配置选项
            return new AnAction[]{new ConfigureToolsAction()};
        }
        
        // 构建子菜单项
        List<AnAction> actions = new ArrayList<>();
        
        // 添加每个工具的菜单项
        for (ExternalTool tool : tools) {
            actions.add(new OpenWithSubAction(tool));
        }
        
        // 添加分隔符
        actions.add(Separator.getInstance());
        
        // 添加配置选项
        actions.add(new ConfigureToolsAction());
        
        return actions.toArray(new AnAction[0]);
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        // 只在有项目、有文件且文件不是目录时显示
        boolean visible = project != null && file != null && !file.isDirectory();
        e.getPresentation().setVisible(visible);
        e.getPresentation().setEnabled(visible);
    }
    
    /**
     * 配置工具的动作
     */
    private static class ConfigureToolsAction extends AnAction {
        
        public ConfigureToolsAction() {
            super("Configure External Tools...");
        }
        
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                // 打开配置页面
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                        .showSettingsDialog(project, "External Tool Opener");
            }
        }
    }
}
