package dev.artha.core;

import java.util.Map;

/**
 * Manages application configuration from artha.json
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, Object> config;

    private ConfigManager() {
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load configuration from artha.json
     */
    public void loadConfig(Map<String, Object> configData) {
        this.config = configData;
    }

    /**
     * Get configuration value by dot notation path
     * Example: get("email.apiKey") -> searches config.email.apiKey
     */
    public Object get(String key) {
        if (config == null) {
            return null;
        }

        String[] parts = key.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) current;
                current = map.get(part);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    /**
     * Get configuration value as String
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get configuration value as Integer
     */
    public Integer getInt(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Get configuration value as Double
     */
    public Double getDouble(String key) {
        Object value = get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    /**
     * Get configuration value as Boolean
     */
    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
}
