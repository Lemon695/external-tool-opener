package com.lemon.externaltool.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.util.FileTypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * External Tool Service
 * 管理外部工具的核心服务类
 */
@Service(Service.Level.APP)
@State(name = "ExternalToolService", storages = @Storage("external_tool_opener.xml"))
public final class ExternalToolService implements PersistentStateComponent<ExternalToolState> {

    private static final Logger LOG = Logger.getInstance(ExternalToolService.class);

    private ExternalToolState myState = new ExternalToolState();

    public ExternalToolService() {
    }

    /**
     * 初始化 - 空实现，用户手动配置
     */
    // Default tools initialization removed as per user requirement for clean manual
    // config.

    /**
     * 获取所有工具
     */
    public List<ExternalTool> getAllTools() {
        return new ArrayList<>(myState.tools);
    }

    /**
     * 获取已启用的工具
     */
    public List<ExternalTool> getEnabledTools() {
        return myState.tools.stream()
                .filter(ExternalTool::isEnabled)
                .sorted(Comparator.comparingInt(ExternalTool::getSortOrder))
                .collect(Collectors.toList());
    }

    /**
     * 获取适用于指定文件的工具列表
     */
    public List<ExternalTool> getToolsForFile(VirtualFile file) {
        if (file == null) {
            LOG.info("getToolsForFile called with null file");
            return new ArrayList<>();
        }

        String extension = FileTypeUtils.getFileExtension(file);
        List<ExternalTool> enabledTools = getEnabledTools();
        List<ExternalTool> matchingTools = enabledTools.stream()
                .filter(tool -> tool.supportsExtension(extension))
                .collect(Collectors.toList());

        LOG.info("File: " + file.getName() + ", Extension: " + extension +
                ", Enabled tools: " + enabledTools.size() +
                ", Matching tools: " + matchingTools.size());

        return matchingTools;
    }

    /**
     * Replaced by ToolExecutionService.execute()
     * Persistence service should not handle execution.
     */

    /**
     * 保存工具配置
     */
    public void saveToolConfig(ExternalTool tool) {
        if (tool == null)
            return;

        // 查找是否已存在
        int index = -1;
        for (int i = 0; i < myState.tools.size(); i++) {
            if (myState.tools.get(i).getId().equals(tool.getId())) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            // 更新现有工具
            myState.tools.set(index, tool);
        } else {
            // 添加新工具
            tool.setSortOrder(myState.tools.size());
            myState.tools.add(tool);
        }
    }

    /**
     * 删除工具配置
     */
    public void deleteToolConfig(String toolId) {
        myState.tools.removeIf(tool -> tool.getId().equals(toolId));
    }

    /**
     * 根据ID获取工具
     */
    @Nullable
    public ExternalTool getToolById(String toolId) {
        return myState.tools.stream()
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
        this.myState.tools = new ArrayList<>(orderedTools);
    }

    /**
     * 获取应用级实例
     */
    public static ExternalToolService getInstance() {
        return ApplicationManager.getApplication().getService(ExternalToolService.class);
    }

    // PersistentStateComponent接口实现

    @Nullable
    @Override
    public ExternalToolState getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull ExternalToolState state) {
        this.myState = state;
        deduplicateIds();
    }

    @Override
    public void noStateLoaded() {
        // Do nothing, fresh start
    }

    // XML序列化支持的getter/setter

    public List<ExternalTool> getTools() {
        return myState.tools;
    }

    /**
     * 确保所有工具ID唯一（修复可能的配置损坏）
     */
    private void deduplicateIds() {
        if (myState.tools == null)
            return;

        java.util.Set<String> ids = new java.util.HashSet<>();
        for (ExternalTool tool : myState.tools) {
            // 如果ID为空或重复，生成新ID
            if (tool.getId() == null || ids.contains(tool.getId())) {
                tool.setId(UUID.randomUUID().toString());
                LOG.info("Regenerated ID for tool: " + tool.getName());
            }
            ids.add(tool.getId());
        }
    }

    public void setTools(List<ExternalTool> tools) {
        myState.tools = new ArrayList<>(tools);
        deduplicateIds();
    }
}
