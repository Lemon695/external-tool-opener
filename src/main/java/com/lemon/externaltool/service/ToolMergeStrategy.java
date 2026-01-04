package com.lemon.externaltool.service;

import com.intellij.openapi.diagnostic.Logger;
import com.lemon.externaltool.model.DetectedTool;
import com.lemon.externaltool.model.ExternalTool;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tool merge strategy for combining detected tools with user configuration
 */
public class ToolMergeStrategy {
    private static final Logger LOG = Logger.getInstance(ToolMergeStrategy.class);

    /**
     * Merge detected tools with existing user configuration
     * 
     * Strategy:
     * 1. Use name as primary key for deduplication
     * 2. Preserve user's existing tools
     * 3. Update paths for existing tools if detected
     * 4. Add new detected tools as disabled by default
     * 5. Preserve user's enabled status and extension configuration
     */
    public List<ExternalTool> merge(List<DetectedTool> detected, List<ExternalTool> existing) {
        Map<String, ExternalTool> resultMap = new LinkedHashMap<>();

        // Step 1: Load existing configuration (preserve user settings)
        for (ExternalTool tool : existing) {
            String key = tool.getName().toLowerCase();
            resultMap.put(key, tool);
        }

        LOG.info("Merging: " + detected.size() + " detected tools with " + existing.size() + " existing tools");

        // Step 2: Merge detected tools
        int updated = 0;
        int added = 0;

        for (DetectedTool detectedTool : detected) {
            String key = detectedTool.getName().toLowerCase();

            if (resultMap.containsKey(key)) {
                // Tool already exists: update path if available, preserve other settings
                ExternalTool existingTool = resultMap.get(key);
                if (detectedTool.isAvailable() && detectedTool.getDetectedPath() != null) {
                    existingTool.setExecutablePath(detectedTool.getDetectedPath());
                    updated++;
                    LOG.info("Updated path for existing tool: " + existingTool.getName());
                }
            } else {
                // New tool: add but default to disabled
                if (detectedTool.isAvailable()) {
                    ExternalTool newTool = convertToExternalTool(detectedTool);
                    newTool.setEnabled(false); // User must explicitly enable
                    resultMap.put(key, newTool);
                    added++;
                    LOG.info("Added new tool (disabled): " + newTool.getName());
                }
            }
        }

        LOG.info("Merge complete: " + updated + " updated, " + added + " added");

        return new ArrayList<>(resultMap.values());
    }

    /**
     * Convert DetectedTool to ExternalTool
     */
    private ExternalTool convertToExternalTool(DetectedTool detected) {
        ExternalTool tool = new ExternalTool();
        tool.setName(detected.getName());
        tool.setExecutablePath(detected.getDetectedPath());
        tool.setEnabled(false); // Default to disabled

        // Set default extensions from definition
        List<String> extensions = detected.getDefinition().getExtensions();
        if (extensions != null && !extensions.isEmpty()) {
            tool.setSupportedExtensions(extensions);
        } else {
            tool.setSupportedExtensions(new ArrayList<>()); // Empty = all files
        }

        return tool;
    }

    /**
     * Get only available detected tools
     */
    public List<DetectedTool> getAvailableTools(List<DetectedTool> detected) {
        return detected.stream()
                .filter(DetectedTool::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Get unavailable detected tools
     */
    public List<DetectedTool> getUnavailableTools(List<DetectedTool> detected) {
        return detected.stream()
                .filter(t -> !t.isAvailable())
                .collect(Collectors.toList());
    }
}
