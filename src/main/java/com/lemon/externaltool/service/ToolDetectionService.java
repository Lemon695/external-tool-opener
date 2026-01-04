package com.lemon.externaltool.service;

import com.intellij.openapi.diagnostic.Logger;
import com.lemon.externaltool.model.DetectedTool;
import com.lemon.externaltool.model.Platform;
import com.lemon.externaltool.model.ToolDefinition;
import com.lemon.externaltool.model.ToolRegistry;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool detection service
 */
public class ToolDetectionService {
    private static final Logger LOG = Logger.getInstance(ToolDetectionService.class);

    /**
     * Detect all available tools from registry
     */
    public List<DetectedTool> detectAvailableTools() {
        List<DetectedTool> detected = new ArrayList<>();

        try {
            ToolRegistry registry = ToolRegistryLoader.loadDefault();
            LOG.info("Loaded tool registry with " + registry.getTools().size() + " tools");

            for (ToolDefinition definition : registry.getTools()) {
                DetectedTool tool = detectTool(definition);
                if (tool != null) {
                    detected.add(tool);
                    if (tool.isAvailable()) {
                        LOG.info("Detected: " + tool.getName() + " at " + tool.getDetectedPath());
                    }
                }
            }

            LOG.info("Detection complete: " + detected.size() + " tools processed, " +
                    detected.stream().filter(DetectedTool::isAvailable).count() + " available");

        } catch (Exception e) {
            LOG.error("Failed to detect tools", e);
        }

        return detected;
    }

    /**
     * Detect a single tool
     */
    public DetectedTool detectTool(ToolDefinition definition) {
        List<String> paths = definition.getPathsForCurrentPlatform();

        for (String pathTemplate : paths) {
            String path = expandPath(pathTemplate);
            if (validateToolPath(path)) {
                return new DetectedTool(definition, path, true);
            }
        }

        // Tool not found, but still return it as unavailable
        return new DetectedTool(definition, null, false);
    }

    /**
     * Validate if a tool path exists and is accessible
     */
    public boolean validateToolPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);

            // For .app bundles on Mac, check if directory exists
            if (Platform.current() == Platform.MAC && path.endsWith(".app")) {
                return Files.exists(filePath) && Files.isDirectory(filePath);
            }

            // For executables, check if file exists
            return Files.exists(filePath) && (Files.isDirectory(filePath) || Files.isExecutable(filePath));

        } catch (Exception e) {
            LOG.debug("Path validation failed for: " + path, e);
            return false;
        }
    }

    /**
     * Expand path template (e.g., replace {user} with actual username)
     */
    private String expandPath(String pathTemplate) {
        if (pathTemplate == null) {
            return null;
        }

        String expanded = pathTemplate;

        // Replace {user} with actual username
        if (expanded.contains("{user}")) {
            String username = System.getProperty("user.name");
            expanded = expanded.replace("{user}", username);
        }

        // Replace {home} with user home directory
        if (expanded.contains("{home}")) {
            String home = System.getProperty("user.home");
            expanded = expanded.replace("{home}", home);
        }

        return expanded;
    }

    /**
     * Get current platform
     */
    public Platform getCurrentPlatform() {
        return Platform.current();
    }
}
