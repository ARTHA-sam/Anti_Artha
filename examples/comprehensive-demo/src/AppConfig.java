package demo;

import dev.artha.annotations.ConfigValue;

/**
 * Service demonstrating configuration value injection
 */
public class AppConfig {

    @ConfigValue("app.name")
    private String appName;

    @ConfigValue("app.version")
    private String appVersion;

    @ConfigValue("app.maxProductsPerPage")
    private int maxProductsPerPage;

    @ConfigValue("features.enableLogging")
    private boolean loggingEnabled;

    @ConfigValue("features.debugMode")
    private boolean debugMode;

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public int getMaxProductsPerPage() {
        return maxProductsPerPage;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public String getInfo() {
        return String.format("%s v%s (Max products: %d, Logging: %s, Debug: %s)",
                appName, appVersion, maxProductsPerPage, loggingEnabled, debugMode);
    }
}
