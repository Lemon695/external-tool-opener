package com.lemon.externaltool.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.util.FileTypeUtils;
import com.lemon.externaltool.util.ProcessExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * External Tool Service
 * 管理外部工具的核心服务类
 */
@State(
        name = "ExternalToolService",
        storages = @Storage("externalTools.xml")
)
public class ExternalToolService implements PersistentStateComponent<ExternalToolService> {
    
    private static final Logger LOG = Logger.getInstance(ExternalToolService.class);
    
    private List<ExternalTool> tools = new ArrayList<>();
    private final Project project;
    
    public ExternalToolService(Project project) {
        this.project = project;
        initializeDefaultTools();
    }
    
    /**
     * 初始化默认工具配置
     */
    private void initializeDefaultTools() {
        if (tools.isEmpty()) {
            // Windows平台默认配置
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                addDefaultTool("Typora", "C:\\Program Files\\Typora\\Typora.exe", ".md", ".markdown");
                addDefaultTool("Notepad++", "C:\\Program Files\\Notepad++\\notepad++.exe", ".txt", ".log", ".json", ".xml");
                addDefaultTool("VSCode", "C:\\Program Files\\Microsoft VS Code\\Code.exe", ".*");
            }
            // macOS平台默认配置
            else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                addDefaultTool("Typora", "/Applications/Typora.app", ".md", ".markdown");
                addDefaultTool("VSCode", "/Applications/Visual Studio Code.app", ".*");
                addDefaultTool("Sublime Text", "/Applications/Sublime Text.app", ".*");
            }
            // Linux平台默认配置
            else {
                addDefaultTool("Typora", "/usr/bin/typora", ".md", ".markdown");
                addDefaultTool("VSCode", "/usr/bin/code", ".*");
                addDefaultTool("gedit", "/usr/bin/gedit", ".txt", ".log");
            }
        }
    }
    
    /**
     * 添加默认工具（仅当工具路径存在时）
     */
    private void addDefaultTool(String name, String path, String... extensions) {
        java.io.File file = new java.io.File(path);
        if (file.exists()) {
            ExternalTool tool = new ExternalTool(name, path);
            for (String ext : extensions) {
                tool.addSupportedExtension(ext);
            }
            tool.setSortOrder(tools.size());
            tools.add(tool);
            LOG.info("Added default tool: " + name);
        }
    }
    
    /**
     * 获取所有工具
     */
    public List<ExternalTool> getAllTools() {
        return new ArrayList<>(tools);
    }
    
    /**
     * 获取已启用的工具
     */
    public List<ExternalTool> getEnabledTools() {
        return tools.stream()
                .filter(ExternalTool::isEnabled)
                .sorted(Comparator.comparingInt(ExternalTool::getSortOrder))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取适用于指定文件的工具列表
     */
    public List<ExternalTool> getToolsForFile(VirtualFile file) {
        if (file == null) {
            return new ArrayList<>();
        }
        
        String extension = FileTypeUtils.getFileExtension(file);
        return getEnabledTools().stream()
                .filter(tool -> tool.supportsExtension(extension))
                .collect(Collectors.toList());
    }
    
    /**
     * 使用指定工具打开文件
     */
    public void openFileWith(VirtualFile file, ExternalTool tool) {
        if (file == null || tool == null) {
            LOG.warn("Cannot open file: file or tool is null");
            return;
        }
        
        try {
            String filePath = file.getPath();
            ProcessExecutor.openWithTool(tool, filePath, project);
            LOG.info("Opened file: " + filePath + " with tool: " + tool.getName());
        } catch (Exception e) {
            LOG.error("Failed to open file with tool: " + tool.getName(), e);
            ProcessExecutor.showErrorNotification(
                    project,
                    "Failed to open file with " + tool.getName(),
                    e.getMessage()
            );
        }
    }
    
    /**
     * 保存工具配置
     */
    public void saveToolConfig(ExternalTool tool) {
        if (tool == null) {
            return;
        }
        
        // 查找是否已存在
        int index = -1;
        for (int i = 0; i < tools.size(); i++) {
            if (tools.get(i).getId().equals(tool.getId())) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            // 更新现有工具
            tools.set(index, tool);
        } else {
            // 添加新工具
            tool.setSortOrder(tools.size());
            tools.add(tool);
        }
        
        LOG.info("Saved tool config: " + tool.getName());
    }
    
    /**
     * 删除工具配置
     */
    public void deleteToolConfig(String toolId) {
        tools.removeIf(tool -> tool.getId().equals(toolId));
        LOG.info("Deleted tool config: " + toolId);
    }
    
    /**
     * 根据ID获取工具
     */
    @Nullable
    public ExternalTool getToolById(String toolId) {
        return tools.stream()
                .filter(tool -> tool.getId().equals(toolId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 更新工具排序
     */
    public void updateToolOrder(List<ExternalTool> orderedTools) {
        for (int i = 0; i < orderedTools.size(); i++) {
            orderedTools.get(i).setSortOrder(i);
        }
        this.tools = new ArrayList<>(orderedTools);
    }
    
    /**
     * 获取项目实例
     */
    public static ExternalToolService getInstance(Project project) {
        return project.getService(ExternalToolService.class);
    }
    
    // PersistentStateComponent接口实现
    
    @Nullable
    @Override
    public ExternalToolService getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull ExternalToolService state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // XML序列化支持的getter/setter
    
    public List<ExternalTool> getTools() {
        return tools;
    }
    
    public void setTools(List<ExternalTool> tools) {
        this.tools = tools;
    }
}
