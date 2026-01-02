package com.lemon.externaltool.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.lemon.externaltool.service.ExternalToolService;
import com.lemon.externaltool.ui.ToolConfigPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * External Tool Configurable
 * 插件配置页面入口
 */
public class ExternalToolConfigurable implements Configurable {
    
    private final Project project;
    private ToolConfigPanel configPanel;
    
    public ExternalToolConfigurable(Project project) {
        this.project = project;
    }
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "External Tool Opener";
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        if (configPanel == null) {
            configPanel = new ToolConfigPanel(project);
        }
        return configPanel.getMainPanel();
    }
    
    @Override
    public boolean isModified() {
        return configPanel != null && configPanel.isModified();
    }
    
    @Override
    public void apply() {
        if (configPanel != null) {
            configPanel.apply();
        }
    }
    
    @Override
    public void reset() {
        if (configPanel != null) {
            configPanel.reset();
        }
    }
    
    @Override
    public void disposeUIResources() {
        configPanel = null;
    }
}
