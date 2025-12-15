import dev.artha.annotations.*;
import dev.artha.db.Database;

public class CleanupTask {

    @ConfigValue("app.name")
    private String appName;

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanup() {
        System.out.println("[" + appName + "] Running cleanup...");

        // Use Database.getInstance() directly
        Database.getInstance().execute(
                "DELETE FROM posts WHERE " +
                        "created_at < DATE_SUB(NOW(), INTERVAL 180 DAY) AND likes_count = 0");

        System.out.println("[" + appName + "] Cleanup done!");
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void heartbeat() {
        System.out.println("[" + appName + "] ❤️ Server running...");
    }
}
