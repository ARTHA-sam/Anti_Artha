package dev.artha.deps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ARTHA Dependency Loader
 * Downloads JARs from Maven Central and adds them to classpath
 */
public class DependencyLoader {
    private static final String LIBS_DIR = ".artha/libs";
    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2";

    private static final Map<String, String> DEPENDENCY_MAP = new HashMap<>();

    static {
        // Dependency name -> groupId:artifactId:version
        DEPENDENCY_MAP.put("lombok", "org.projectlombok:lombok:1.18.42");
        DEPENDENCY_MAP.put("postgresql", "org.postgresql:postgresql:42.7.3");
        DEPENDENCY_MAP.put("mysql-connector", "com.mysql:mysql-connector-j:8.3.0");
        DEPENDENCY_MAP.put("mysql", "com.mysql:mysql-connector-j:8.3.0"); // Alias for convenience
        DEPENDENCY_MAP.put("gson", "com.google.code.gson:gson:2.10.1");
        DEPENDENCY_MAP.put("jackson", "com.fasterxml.jackson.core:jackson-databind:2.16.1");
        DEPENDENCY_MAP.put("jackson-core", "com.fasterxml.jackson.core:jackson-core:2.16.1");
        DEPENDENCY_MAP.put("jackson-annotations", "com.fasterxml.jackson.core:jackson-annotations:2.16.1");
        DEPENDENCY_MAP.put("sqlite", "org.xerial:sqlite-jdbc:3.45.1.0");
        DEPENDENCY_MAP.put("h2", "com.h2database:h2:2.2.224");
        DEPENDENCY_MAP.put("logback", "ch.qos.logback:logback-classic:1.4.14");

        // OpenAPI / Swagger
        DEPENDENCY_MAP.put("javalin-openapi", "io.javalin.community.openapi:javalin-openapi-plugin:6.1.0");
        DEPENDENCY_MAP.put("javalin-swagger", "io.javalin.community.openapi:javalin-swagger-plugin:6.1.0");
        DEPENDENCY_MAP.put("javalin-redoc", "io.javalin.community.openapi:javalin-redoc-plugin:6.1.0");
        DEPENDENCY_MAP.put("swagger-core", "io.swagger.core.v3:swagger-core-jakarta:2.2.20");
        DEPENDENCY_MAP.put("swagger-models", "io.swagger.core.v3:swagger-models-jakarta:2.2.20");
        DEPENDENCY_MAP.put("swagger-annotations", "io.swagger.core.v3:swagger-annotations-jakarta:2.2.20");
    }

    public static void loadDependencies(List<String> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        // Handle transitive dependencies for Jackson
        if (dependencies.contains("jackson")) {
            if (!dependencies.contains("jackson-core"))
                dependencies.add("jackson-core");
            if (!dependencies.contains("jackson-annotations"))
                dependencies.add("jackson-annotations");
        }

        System.out.println("📦 Loading dependencies...\n");

        File libsDir = new File(LIBS_DIR);
        if (!libsDir.exists()) {
            libsDir.mkdirs();
        }

        for (int i = 0; i < dependencies.size(); i++) {
            String depName = dependencies.get(i);
            String artifact = DEPENDENCY_MAP.get(depName.toLowerCase());

            if (artifact == null) {
                System.out.println("  ⚠️  Unknown dependency: " + depName + " — skipping");
                continue;
            }

            try {
                downloadAndLoad(depName, artifact);
            } catch (Exception e) {
                System.err.println("  ❌ Failed to load " + depName + ": " + e.getMessage());
            }
        }

        System.out.println();
    }

    private static void downloadAndLoad(String name, String artifact) throws Exception {
        String[] parts = artifact.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid artifact format: " + artifact);
        }

        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];

        String fileName = artifactId + "-" + version + ".jar";
        File jarFile = new File(LIBS_DIR, fileName);

        // Download if not exists
        if (!jarFile.exists()) {
            System.out.println("  ⬇ Downloading " + name + "...");

            String groupPath = groupId.replace('.', '/');
            String jarUrl = String.format("%s/%s/%s/%s/%s",
                    MAVEN_CENTRAL, groupPath, artifactId, version, fileName);

            downloadFile(jarUrl, jarFile);
            System.out.println("    ✓ Downloaded " + fileName);
        } else {
            System.out.println("  ✓ Found " + fileName);
        }

        // Add to classpath
        addToClasspath(jarFile);
    }

    private static void downloadFile(String urlString, File destination) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        try (InputStream in = url.openStream();
                FileOutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void addToClasspath(File jarFile) throws Exception {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(classLoader, jarFile.toURI().toURL());
    }

    public static void addDependency(String name, String artifact) {
        DEPENDENCY_MAP.put(name.toLowerCase(), artifact);
    }
}
