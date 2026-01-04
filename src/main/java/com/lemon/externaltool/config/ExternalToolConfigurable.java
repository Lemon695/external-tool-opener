package com.lemon.externaltool.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.lemon.externaltool.ui.ToolConfigPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * External Tool Configurable (Global)
 * 插件全局配置页面入口
 */
public class ExternalToolConfigurable implements Configurable {

    // UI Panel instance
    private ToolConfigPanel configPanel;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "External Tool Opener";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (configPanel == null) {
            configPanel = new ToolConfigPanel();
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
