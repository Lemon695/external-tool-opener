package com.lemon.externaltool.model;

/**
 * Platform enumeration for cross-platform support
 */
public enum Platform {
    WINDOWS("windows"),
    MAC("mac"),
    LINUX("linux");

    private final String key;

    Platform(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * Get the current platform
     */
    public static Platform current() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return WINDOWS;
        } else if (os.contains("mac")) {
            return MAC;
        } else {
            return LINUX;
        }
    }

    @Override
    public String toString() {
        return key;
    }
}
