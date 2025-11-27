package dev.artha.core;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.RequestImpl;
import dev.artha.http.Response;
import dev.artha.http.ResponseImpl;
import io.javalin.Javalin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * ARTHA Runtime - Main server entry point
 */
public class Runtime {

    public static void main(String[] args) {
        printBanner();

        // Get port from CLI -Dartha.port or default 8080
        int port = Integer.parseInt(System.getProperty("artha.port", "8080"));
        System.out.println("ℹ️  Using port: " + port + "\n");

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            // Configure Jackson as the default JSON mapper
            config.jsonMapper(new io.javalin.json.JavalinJackson());

            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        });

        // Scan for @Step annotations
        System.out.println("🔍 Scanning for routes...\n");

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forJavaClassPath())
                        .setScanners(Scanners.TypesAnnotated));

        // Scan for @Step annotations (Classes)
        Set<Class<?>> stepClasses = reflections.getTypesAnnotatedWith(Step.class);

        // Scan for @Step annotations (Methods)
        Set<Method> stepMethods = reflections.getMethodsAnnotatedWith(Step.class);

        if (stepClasses.isEmpty() && stepMethods.isEmpty()) {
            System.out.println("⚠️  No @Step annotations found!");
            System.out.println("   Make sure your classes or methods use @Step annotation\n");
        }

        // Register Class-based routes (Legacy)
        for (Class<?> clazz : stepClasses) {
            registerClassRoute(app, clazz);
        }

        // Register Method-based routes (New)
        for (Method method : stepMethods) {
            registerMethodRoute(app, method);
        }

        app.start(port);

        System.out.println("\n✓ Server started at http://localhost:" + port + "\n");
        System.out.println("Press Ctrl+C to stop\n");
    }

    private static void registerClassRoute(Javalin app, Class<?> clazz) {
        try {
            Method handleMethod = findHandlerMethod(clazz);

            if (handleMethod == null) {
                // If the class has method-level annotations, we shouldn't warn about missing
                // handle()
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(Step.class))
                        return;
                }
                System.out.println("  ⚠️  No handler method found in " + clazz.getSimpleName()
                        + " (expected 'handle' or a single method)");
                return;
            }

            Step step = clazz.getAnnotation(Step.class);
            String path = step.path();
            String method = step.method().toUpperCase();

            System.out.println(
                    "  " + method + "  " + path + " → " + clazz.getSimpleName() + "." + handleMethod.getName() + "()");

            registerHandler(app, method, path, ctx -> handleRequest(ctx, clazz, handleMethod));
        } catch (Exception e) {
            System.err.println("❌ Failed to register " + clazz.getName());
            e.printStackTrace();
        }
    }

    private static Method findHandlerMethod(Class<?> clazz) {
        // 1. Try standard "handle" method
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals("handle"))
                return m;
        }

        // 2. If only one method is defined, assume it's the handler
        // Filter out synthetic methods (like lambda bodies)
        java.util.List<Method> userMethods = new java.util.ArrayList<>();
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isSynthetic())
                userMethods.add(m);
        }

        if (userMethods.size() == 1) {
            return userMethods.get(0);
        }

        return null;
    }

    private static void registerMethodRoute(Javalin app, Method method) {
        try {
            Step step = method.getAnnotation(Step.class);
            String path = step.path();
            String httpMethod = step.method().toUpperCase();
            Class<?> clazz = method.getDeclaringClass();

            System.out.println(
                    "  " + httpMethod + "  " + path + " → " + clazz.getSimpleName() + "." + method.getName() + "()");

            registerHandler(app, httpMethod, path, ctx -> handleRequest(ctx, clazz, method));
        } catch (Exception e) {
            System.err.println("❌ Failed to register " + method.getName());
            e.printStackTrace();
        }
    }

    private static void registerHandler(Javalin app, String method, String path, io.javalin.http.Handler handler) {
        switch (method) {
            case "GET":
                app.get(path, handler);
                break;
            case "POST":
                app.post(path, handler);
                break;
            case "PUT":
                app.put(path, handler);
                break;
            case "DELETE":
                app.delete(path, handler);
                break;
            case "PATCH":
                app.patch(path, handler);
                break;
        }
    }

    private static void handleRequest(io.javalin.http.Context ctx, Class<?> clazz, Method method) {
        try {
            // TODO: Dependency Injection support
            java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true); // Allow package-private classes
            Object instance = constructor.newInstance();
            invokeAndRespond(ctx, instance, method);
        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    private static void invokeAndRespond(io.javalin.http.Context ctx, Object instance, Method method) throws Exception {
        Request req = new RequestImpl(ctx);
        Response res = new ResponseImpl(ctx);

        // Smart Parameter Injection
        java.lang.reflect.Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            if (type == Request.class) {
                args[i] = req;
            } else if (type == Response.class) {
                args[i] = res;
            } else {
                // TODO: Future - Inject body, query params, etc. based on type/name
                args[i] = null;
            }
        }

        method.setAccessible(true); // Allow calling package-private methods
        Object result = method.invoke(instance, args);

        // Auto-serialize if not already handled
        if (result != null) {
            if (result instanceof String) {
                ctx.result((String) result);
            } else {
                ctx.json(result);
            }
        }
    }

    private static void handleError(io.javalin.http.Context ctx, Exception e) {
        // Use HashMap instead of Map.of to allow null values
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Internal server error");
        errorResponse.put("path", ctx.path() != null ? ctx.path() : "unknown");

        ctx.status(500).json(errorResponse);
        e.printStackTrace();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("   █████╗ ██████╗ ████████╗██╗  ██╗ █████╗ ");
        System.out.println("  ██╔══██╗██╔══██╗╚══██╔══╝██║  ██║██╔══██╗");
        System.out.println("  ███████║██████╔╝   ██║   ███████║███████║");
        System.out.println("  ██╔══██║██╔══██╗   ██║   ██╔══██║██╔══██║");
        System.out.println("  ██║  ██║██║  ██║   ██║   ██║  ██║██║  ██║");
        System.out.println("  ╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝");
        System.out.println();
        System.out.println("  Simple Java Backend Framework for Students");
        System.out.println("  Version 0.1.0");
        System.out.println();
    }
}
