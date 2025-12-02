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
        instances.put(type, instance);
    }

    /**
     * Get or create an instance of the specified type.
     * Automatically injects dependencies marked with @Inject.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        // Check if instance already exists (singleton)
        Object existing = instances.get(type);
        if (existing != null) {
            return (T) existing;
        }

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

        // Inject all fields marked with @Inject
        for (Field field : clazz.getDeclaredFields()) {
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
        }
    }

    /**
     * Clear all instances (useful for testing).
     */
    public void clear() {
        instances.clear();
    }
}
