package com.lemon.externaltool.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool definition from registry
 */
public class ToolDefinition {
    private String id;
    private String name;
    private String category;
    private Map<String, List<String>> platforms = new HashMap<>();
    private List<String> extensions = new ArrayList<>();
    private int priority = 5;
    private boolean userDefined = false;

    public ToolDefinition() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String, List<String>> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Map<String, List<String>> platforms) {
        this.platforms = platforms;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isUserDefined() {
        return userDefined;
    }

    public void setUserDefined(boolean userDefined) {
        this.userDefined = userDefined;
    }

    /**
     * Get paths for current platform
     */
    public List<String> getPathsForCurrentPlatform() {
        Platform platform = Platform.current();
        return platforms.getOrDefault(platform.getKey(), new ArrayList<>());
    }

    /**
     * Get paths for specific platform
     */
    public List<String> getPathsForPlatform(Platform platform) {
        return platforms.getOrDefault(platform.getKey(), new ArrayList<>());
    }
}
