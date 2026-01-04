package com.lemon.externaltool.model;

import java.time.LocalDateTime;

/**
 * Detected tool with availability information
 */
public class DetectedTool {
    private final ToolDefinition definition;
    private final String detectedPath;
    private final boolean available;
    private LocalDateTime lastDetected;

    public DetectedTool(ToolDefinition definition, String detectedPath, boolean available) {
        this.definition = definition;
        this.detectedPath = detectedPath;
        this.available = available;
        this.lastDetected = LocalDateTime.now();
    }

    public ToolDefinition getDefinition() {
        return definition;
    }

    public String getDetectedPath() {
        return detectedPath;
    }

    public boolean isAvailable() {
        return available;
    }

    public LocalDateTime getLastDetected() {
        return lastDetected;
    }

    public String getName() {
        return definition.getName();
    }

    public String getId() {
        return definition.getId();
    }
}
