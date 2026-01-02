package com.lemon.externaltool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * External Tool Model
 * 表示一个外部工具的配置信息
 */
public class ExternalTool implements Serializable, Cloneable {
    
    private String id;
    private String name;
    private String executablePath;
    private List<String> supportedExtensions;
    private String iconPath;
    private String commandTemplate;
    private boolean isDefault;
    private int sortOrder;
    private boolean enabled;
    
    public ExternalTool() {
        this.id = UUID.randomUUID().toString();
        this.supportedExtensions = new ArrayList<>();
        this.commandTemplate = "\"{path}\" \"{file}\"";
        this.enabled = true;
        this.isDefault = false;
        this.sortOrder = 0;
    }
    
    public ExternalTool(String name, String executablePath) {
        this();
        this.name = name;
        this.executablePath = executablePath;
    }
    
    // Getters and Setters
    
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
    
    public String getExecutablePath() {
        return executablePath;
    }
    
    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }
    
    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }
    
    public void setSupportedExtensions(List<String> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
    
    public String getCommandTemplate() {
        return commandTemplate;
    }
    
    public void setCommandTemplate(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 检查此工具是否支持指定的文件扩展名
     */
    public boolean supportsExtension(String extension) {
        if (supportedExtensions == null || supportedExtensions.isEmpty()) {
            return true; // 如果没有配置扩展名，表示支持所有类型
        }
        
        String ext = extension.toLowerCase();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        
        for (String supportedExt : supportedExtensions) {
            String supported = supportedExt.toLowerCase();
            if (!supported.startsWith(".")) {
                supported = "." + supported;
            }
            if (supported.equals(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 添加支持的文件扩展名
     */
    public void addSupportedExtension(String extension) {
        if (supportedExtensions == null) {
            supportedExtensions = new ArrayList<>();
        }
        String ext = extension.toLowerCase();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }
        if (!supportedExtensions.contains(ext)) {
            supportedExtensions.add(ext);
        }
    }
    
    @Override
    public ExternalTool clone() {
        try {
            ExternalTool clone = (ExternalTool) super.clone();
            clone.supportedExtensions = new ArrayList<>(this.supportedExtensions);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String toString() {
        return "ExternalTool{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", executablePath='" + executablePath + '\'' +
                ", supportedExtensions=" + supportedExtensions +
                ", enabled=" + enabled +
                '}';
    }
}
