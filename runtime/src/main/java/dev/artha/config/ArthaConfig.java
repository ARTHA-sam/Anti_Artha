package dev.artha.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ARTHA Configuration Loader
 * Reads artha.json from project root
 * Supports both flat and nested (CLI) formats
 */
public class ArthaConfig {
    private int port = 8080;
    private String env = "dev";
    private List<String> dependencies = new ArrayList<>();

    private static ArthaConfig instance;

    public static ArthaConfig load() {
        if (instance != null) {
            return instance;
        }

        instance = new ArthaConfig();
        File configFile = new File("artha.json");

        if (!configFile.exists()) {
            System.out.println("ℹ️  No artha.json found — using defaults\n");
            return instance;
        }

        try {
            String json = readFile(configFile);
            instance = parseJson(json);

            System.out.println("✓ Loaded artha.json");
            System.out.println("  Port: " + instance.port);
            if (!instance.dependencies.isEmpty()) {
                System.out.println("  Dependencies: " + instance.dependencies);
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("⚠️  Failed to load artha.json: " + e.getMessage());
            System.err.println("   Using defaults\n");
        }

        return instance;
    }

    private static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static ArthaConfig parseJson(String json) {
        ArthaConfig config = new ArthaConfig();

        // Support both formats:
        // 1. Top-level: { "port": 8080 }
        // 2. Nested (CLI format): { "server": { "port": 8080 } }

        // Try nested server.port FIRST (CLI format)
        Pattern serverPortPattern = Pattern.compile("\"server\"\\s*:\\s*\\{[^}]*\"port\"\\s*:\\s*(\\d+)");
        Matcher serverPortMatcher = serverPortPattern.matcher(json);
        if (serverPortMatcher.find()) {
            config.port = Integer.parseInt(serverPortMatcher.group(1));
        } else {
            // Try top-level port as fallback
            Pattern portPattern = Pattern.compile("\"port\"\\s*:\\s*(\\d+)");
            Matcher portMatcher = portPattern.matcher(json);
            if (portMatcher.find()) {
                config.port = Integer.parseInt(portMatcher.group(1));
            }
        }

        // Parse env
        Pattern envPattern = Pattern.compile("\"env\"\\s*:\\s*\"([^\"]+)\"");
        Matcher envMatcher = envPattern.matcher(json);
        if (envMatcher.find()) {
            config.env = envMatcher.group(1);
        }

        // Parse dependencies - support both array and object formats
        // Array format: "dependencies": ["lombok", "postgresql"]
        Pattern depsArrayPattern = Pattern.compile("\"dependencies\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher depsArrayMatcher = depsArrayPattern.matcher(json);
        if (depsArrayMatcher.find()) {
            String depsArray = depsArrayMatcher.group(1);
            Pattern depPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher depMatcher = depPattern.matcher(depsArray);
            while (depMatcher.find()) {
                config.dependencies.add(depMatcher.group(1));
            }
        } else {
            // Object format (CLI): "dependencies": { "lombok": "1.18.30" }
            Pattern depsObjPattern = Pattern.compile("\"dependencies\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher depsObjMatcher = depsObjPattern.matcher(json);
            if (depsObjMatcher.find()) {
                String depsObj = depsObjMatcher.group(1);
                Pattern depNamePattern = Pattern.compile("\"([^\"]+)\"\\s*:");
                Matcher depNameMatcher = depNamePattern.matcher(depsObj);
                while (depNameMatcher.find()) {
                    config.dependencies.add(depNameMatcher.group(1));
                }
            }
        }

        return config;
    }

    public int getPort() {
        return port;
    }

    public String getEnv() {
        return env;
    }

    public List<String> getDependencies() {
        return dependencies != null ? dependencies : new ArrayList<>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
}
