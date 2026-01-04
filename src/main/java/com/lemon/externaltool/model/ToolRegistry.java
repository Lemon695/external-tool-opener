package com.lemon.externaltool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Tool registry containing all tool definitions
 */
public class ToolRegistry {
    private String version;
    private List<ToolDefinition> tools = new ArrayList<>();

    public ToolRegistry() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ToolDefinition> getTools() {
        return tools;
    }

    public void setTools(List<ToolDefinition> tools) {
        this.tools = tools;
    }

    /**
     * Find tool by ID
     */
    public ToolDefinition findById(String id) {
        return tools.stream()
                .filter(t -> id.equals(t.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find tool by name (case-insensitive)
     */
    public ToolDefinition findByName(String name) {
        return tools.stream()
                .filter(t -> name.equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(null);
    }
}
