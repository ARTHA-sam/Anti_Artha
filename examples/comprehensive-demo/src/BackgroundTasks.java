package demo;

import dev.artha.annotations.Scheduled;
import dev.artha.annotations.ConfigValue;

/**
 * Service demonstrating scheduled background tasks
 */
public class BackgroundTasks {

    @ConfigValue("app.name")
    private String appName;

    private int heartbeatCount = 0;
    private int cleanupCount = 0;

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void heartbeat() {
        heartbeatCount++;
        System.out.println("💓 [Heartbeat #" + heartbeatCount + "] " + appName + " is alive!");
    }

    // Runs every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void cleanupTask() {
        cleanupCount++;
        System.out.println("🧹 [Cleanup #" + cleanupCount + "] Running cleanup task...");
    }

    // Runs every 5 seconds (for demo purposes)
    @Scheduled(fixedRate = 5000)
    public void statusCheck() {
        System.out.println("✓ [Status] All systems operational (heartbeats: " + heartbeatCount + ", cleanups: "
                + cleanupCount + ")");
    }
}
