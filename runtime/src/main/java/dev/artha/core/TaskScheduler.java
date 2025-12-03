package dev.artha.core;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages scheduled tasks
 */
public class TaskScheduler {
    private static TaskScheduler instance;
    private ScheduledExecutorService executor;

    private TaskScheduler() {
        executor = Executors.newScheduledThreadPool(5);
    }

    public static synchronized TaskScheduler getInstance() {
        if (instance == null) {
            instance = new TaskScheduler();
        }
        return instance;
    }

    /**
     * Schedule a method to run at fixed rate
     */
    public void scheduleFixedRate(Object instance, Method method, long rateMillis) {
        executor.scheduleAtFixedRate(() -> {
            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (Exception e) {
                System.err.println("Error executing scheduled task " + method.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, rateMillis, TimeUnit.MILLISECONDS);

        System.out.println("  ⏰ Scheduled: " + instance.getClass().getSimpleName() + "." + method.getName()
                + "() every " + rateMillis + "ms");
    }

    /**
     * Shutdown the scheduler
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            System.out.println("🛑 Task scheduler shutdown");
        }
    }
}
