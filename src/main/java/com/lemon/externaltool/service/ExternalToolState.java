package com.lemon.externaltool.service;

import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.lemon.externaltool.model.ExternalTool;

import java.util.ArrayList;
import java.util.List;

/**
 * State class for External Tool Service
 * 单独的状态类，确保序列化更稳定
 */
public class ExternalToolState {

    @Tag("tools")
    @XCollection(elementName = "tool")
    public List<ExternalTool> tools = new ArrayList<>();

    public List<ExternalTool> getTools() {
        return tools;
    }

    public void setTools(List<ExternalTool> tools) {
        this.tools = tools;
    }
}
