package com.lemon.externaltool.service;

import com.lemon.externaltool.model.ToolRegistry;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.InputStream;

public class ToolRegistryLoader {
    
    public static ToolRegistry loadFromResource(String resourcePath) {
        try (InputStream is = ToolRegistryLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(new Constructor(ToolRegistry.class, options));
            return yaml.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tool registry: " + resourcePath, e);
        }
    }
    
    public static ToolRegistry loadDefault() {
        return loadFromResource("/tool-registry.yaml");
    }
}
