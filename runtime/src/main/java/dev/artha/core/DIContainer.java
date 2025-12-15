package dev.artha.core;

import dev.artha.annotations.Inject;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Dependency Injection container with singleton scope.
 */
public class DIContainer {
    private static final DIContainer instance = new DIContainer();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private DIContainer() {
    }

    public static DIContainer getInstance() {
        return instance;
    }

    /**
     * Register a manually created instance.
     */
    public <T> void registerInstance(Class<T> type, T instance) {
        System.out.println("DIContainer: Registering instance for " + type.getName());
        instances.put(type, instance);
    }

    /**
     * Get or create an instance of the specified type.
     * Automatically injects dependencies marked with @Inject.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        // Special handling for Database singleton to ensure we always get the
        // initialized instance
        if (type.getName().equals("dev.artha.db.Database")) {
            try {
                // Use reflection to verify it has getInstance method (avoid compile-time
                // dependency cycle if checked strictly)
                java.lang.reflect.Method getInstance = type.getMethod("getInstance");
                return (T) getInstance.invoke(null);
            } catch (Exception e) {
                System.err.println("⚠️  Failed to get Database instance via singleton: " + e.getMessage());
                // Fallthrough to normal creation if fails (though it shouldn't)
            }
        }

        // Check if instance already exists (singleton)
        Object existing = instances.get(type);
        if (existing != null) {
            System.out.println("DIContainer: Returning existing instance for " + type.getName());
            return (T) existing;
        }

        System.out.println("DIContainer: Creating NEW instance for " + type.getName());

        // Create new instance
        try {
            java.lang.reflect.Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true); // Allow package-private classes
            T newInstance = constructor.newInstance();
            instances.put(type, newInstance);

            // Inject dependencies
            injectDependencies(newInstance);

            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + type.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Inject dependencies into an existing object.
     */
    public void injectDependencies(Object target) {
        Class<?> clazz = target.getClass();

        // Inject all fields marked with @Inject or @ConfigValue
        for (Field field : clazz.getDeclaredFields()) {
            // Handle @Inject
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);
                    Object dependency = get(field.getType());
                    field.set(target, dependency);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to inject " + field.getType().getName() + " into " + clazz.getName() + "."
                                    + field.getName(),
                            e);
                }
            }

            // Handle @ConfigValue
            if (field.isAnnotationPresent(dev.artha.annotations.ConfigValue.class)) {
                try {
                    dev.artha.annotations.ConfigValue annotation = field
                            .getAnnotation(dev.artha.annotations.ConfigValue.class);
                    String configKey = annotation.value();

                    field.setAccessible(true);
                    Object configValue = ConfigManager.getInstance().get(configKey);

                    if (configValue == null) {
                        throw new IllegalStateException("Config key '" + configKey + "' not found in artha.json");
                    }

                    // Convert to appropriate type
                    Object typedValue = convertConfigValue(configValue, field.getType());
                    field.set(target, typedValue);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to inject config value into " + clazz.getName() + "." + field.getName(),
                            e);
                }
            }
        }
    }

    /**
     * Convert config value to the target field type
     */
    private Object convertConfigValue(Object value, Class<?> targetType) {
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == int.class || targetType == Integer.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        } else if (targetType == double.class || targetType == Double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        } else if (targetType == long.class || targetType == Long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        }
        return value;
    }

    /**
     * Clear all instances (useful for testing).
     */
    public void clear() {
        instances.clear();
    }
}
