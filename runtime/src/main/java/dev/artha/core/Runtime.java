package dev.artha.core;

import dev.artha.annotations.Step;
import dev.artha.annotations.Valid;
import dev.artha.http.Request;
import dev.artha.http.RequestImpl;
import dev.artha.http.Response;
import dev.artha.http.ResponseImpl;
import dev.artha.db.Database;
import io.javalin.Javalin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.io.File;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

/**
 * ARTHA Runtime - Main server entry point
 */
public class Runtime {

    private static final Validator validator;

    // Exception handler registry: Exception class -> Handler info
    private static final Map<Class<? extends Exception>, ExceptionHandlerInfo> exceptionHandlers = new HashMap<>();

    private static class ExceptionHandlerInfo {
        Class<?> controllerClass;
        Method handlerMethod;

        ExceptionHandlerInfo(Class<?> controllerClass, Method handlerMethod) {
            this.controllerClass = controllerClass;
            this.handlerMethod = handlerMethod;
        }
    }

    // Internal exception to handle validation failures during request processing
    private static class RequestValidationException extends RuntimeException {
        private final Set<ConstraintViolation<Object>> violations;

        RequestValidationException(Set<ConstraintViolation<Object>> violations) {
            this.violations = violations;
        }

        public Set<ConstraintViolation<Object>> getViolations() {
            return violations;
        }
    }

    static {
        // FIX: HV000183 - Use ParameterMessageInterpolator to avoid needing Jakarta EL
        // dependencies
        try {
            ValidatorFactory factory = Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory();
            validator = factory.getValidator();
        } catch (Exception e) {
            System.err.println("⚠️  CRITICAL: Failed to initialize Validator. Server may not start correctly.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        printBanner();

        // Load artha.json configuration
        Map<String, Object> arthaConfig = loadConfig();

        // Initialize database if configured
        if (arthaConfig.containsKey("database")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> dbConfig = (Map<String, Object>) arthaConfig.get("database");
                Database.getInstance().initialize(dbConfig);
            } catch (Exception e) {
                System.err.println("⚠️  Database initialization failed: " + e.getMessage());
            }
        }

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
                        .setScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));

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

        // Scan and register @ExceptionHandler methods
        Set<Method> exceptionHandlerMethods = reflections
                .getMethodsAnnotatedWith(dev.artha.annotations.ExceptionHandler.class);
        for (Method method : exceptionHandlerMethods) {
            registerExceptionHandler(method);
        }

        if (!exceptionHandlers.isEmpty()) {
            System.out.println("\n🛡️  Registered " + exceptionHandlers.size() + " exception handler(s)\n");
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
            Step methodStep = method.getAnnotation(Step.class);
            String methodPath = methodStep.path();
            String httpMethod = methodStep.method().toUpperCase();
            Class<?> clazz = method.getDeclaringClass();

            // Check for Class-level @Step annotation for path nesting
            String classPath = "";
            if (clazz.isAnnotationPresent(Step.class)) {
                Step classStep = clazz.getAnnotation(Step.class);
                classPath = classStep.path();
            }

            // Combine paths
            String fullPath = combinePaths(classPath, methodPath);

            System.out.println(
                    "  " + httpMethod + "  " + fullPath + " → " + clazz.getSimpleName() + "." + method.getName()
                            + "()");

            registerHandler(app, httpMethod, fullPath, ctx -> handleRequest(ctx, clazz, method));
        } catch (Exception e) {
            System.err.println("❌ Failed to register " + method.getName());
            e.printStackTrace();
        }
    }

    private static String combinePaths(String classPath, String methodPath) {
        if (classPath == null || classPath.isEmpty()) {
            return methodPath;
        }

        // Remove trailing slash from classPath
        if (classPath.endsWith("/")) {
            classPath = classPath.substring(0, classPath.length() - 1);
        }

        // Ensure methodPath starts with slash
        if (!methodPath.startsWith("/")) {
            methodPath = "/" + methodPath;
        }

        return classPath + methodPath;
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
            // Use DI container for instance creation (supports @Inject)
            Object instance = DIContainer.getInstance().get(clazz);

            Request req = new RequestImpl(ctx);
            Response res = new ResponseImpl(ctx);

            // 1. Execute Class-level @Before
            if (clazz.isAnnotationPresent(dev.artha.annotations.Before.class)) {
                executeMiddleware(clazz.getAnnotation(dev.artha.annotations.Before.class).value(), req, res);
            }

            // 2. Execute Method-level @Before
            if (method.isAnnotationPresent(dev.artha.annotations.Before.class)) {
                executeMiddleware(method.getAnnotation(dev.artha.annotations.Before.class).value(), req, res);
            }

            // 3. Invoke Handler
            invokeAndRespond(ctx, instance, method, req, res);

            // 4. Execute Method-level @After
            if (method.isAnnotationPresent(dev.artha.annotations.After.class)) {
                executeMiddleware(method.getAnnotation(dev.artha.annotations.After.class).value(), req, res);
            }

            // 5. Execute Class-level @After
            if (clazz.isAnnotationPresent(dev.artha.annotations.After.class)) {
                executeMiddleware(clazz.getAnnotation(dev.artha.annotations.After.class).value(), req, res);
            }

        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    private static void executeMiddleware(Class<? extends dev.artha.http.Middleware>[] middlewares, Request req,
            Response res) throws Exception {
        for (Class<? extends dev.artha.http.Middleware> middlewareClass : middlewares) {
            // Use DI container to get middleware instance
            dev.artha.http.Middleware middleware = DIContainer.getInstance().get(middlewareClass);
            middleware.apply(req, res);
        }
    }

    private static void invokeAndRespond(io.javalin.http.Context ctx, Object instance, Method method, Request req,
            Response res) throws Exception {
        // Track injected connection for auto-closing
        Connection injectedConnection = null;

        // Smart Parameter Injection
        java.lang.reflect.Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            java.lang.reflect.Parameter param = params[i];
            Class<?> type = param.getType();

            if (type == Request.class) {
                args[i] = req;
            } else if (type == Response.class) {
                args[i] = res;
            } else if (type == Connection.class) {
                // Inject database connection
                if (Database.getInstance().isInitialized()) {
                    injectedConnection = Database.getInstance().getConnection();
                    args[i] = injectedConnection;
                } else {
                    throw new IllegalStateException("Database not configured! Add database section to artha.json");
                }
            } else {
                // Advanced Injection: Parse POJO from request body
                try {
                    String body = ctx.body();
                    if (body != null && !body.trim().isEmpty()) {
                        // Use Jackson to deserialize JSON to POJO
                        ObjectMapper mapper = new ObjectMapper();
                        Object pojo = mapper.readValue(body, type);

                        // Validate if @Valid annotation is present on the method parameter
                        if (param.isAnnotationPresent(Valid.class)) {
                            @SuppressWarnings("unchecked")
                            Set<ConstraintViolation<Object>> violations = validator.validate(pojo);
                            if (!violations.isEmpty()) {
                                throw new RequestValidationException(violations);
                            }
                        }
                        args[i] = pojo;
                    } else {
                        args[i] = null;
                    }
                } catch (RequestValidationException e) {
                    throw e; // Re-throw validation exceptions to be caught by handleError
                } catch (Exception e) {
                    // If parsing fails, throw a 400 Bad Request
                    throw new IllegalArgumentException("Invalid request body: " + e.getMessage());
                }
            }
        }

        try {
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
        } finally {
            // CRITICAL: Always close the injected database connection
            if (injectedConnection != null) {
                try {
                    injectedConnection.close();
                } catch (Exception e) {
                    System.err.println("Warning: Failed to close database connection: " + e.getMessage());
                }
            }
        }
    }

    private static void handleError(io.javalin.http.Context ctx, Exception e) {
        // Unwrap InvocationTargetException to get the real cause
        Exception actualException = e;
        if (e.getCause() instanceof Exception) {
            actualException = (Exception) e.getCause();
        }

        // Try to find a matching exception handler
        ExceptionHandlerInfo handler = findExceptionHandler(actualException.getClass());

        if (handler != null) {
            try {
                // Invoke the exception handler
                Object controllerInstance = DIContainer.getInstance().get(handler.controllerClass);
                Request req = new RequestImpl(ctx);
                Response res = new ResponseImpl(ctx);

                handler.handlerMethod.setAccessible(true);
                Object result = handler.handlerMethod.invoke(controllerInstance, actualException, req, res);

                if (result != null) {
                    if (result instanceof String) {
                        ctx.result((String) result);
                    } else {
                        ctx.json(result);
                    }
                }
                return;
            } catch (Exception handlerException) {
                System.err.println("Exception handler failed: " + handlerException.getMessage());
                handlerException.printStackTrace();
                // Fall through to default error handling
            }
        }

        // Default error handling
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);

        // Check for our custom validation exception
        if (actualException instanceof RequestValidationException) {
            RequestValidationException ve = (RequestValidationException) actualException;

            errorResponse.put("message", "Validation failed");

            List<Map<String, String>> violations = new ArrayList<>();
            for (ConstraintViolation<?> violation : ve.getViolations()) {
                Map<String, String> violationMap = new HashMap<>();
                violationMap.put("field", violation.getPropertyPath().toString());
                violationMap.put("message", violation.getMessage());
                violations.add(violationMap);
            }
            errorResponse.put("violations", violations);

            ctx.status(400).json(errorResponse);
        } else {
            // Handle other errors
            errorResponse.put("message",
                    actualException.getMessage() != null ? actualException.getMessage() : "Internal server error");
            errorResponse.put("path", ctx.path() != null ? ctx.path() : "unknown");
            ctx.status(500).json(errorResponse);
            actualException.printStackTrace();
        }
    }

    private static void registerExceptionHandler(Method method) {
        dev.artha.annotations.ExceptionHandler annotation = method
                .getAnnotation(dev.artha.annotations.ExceptionHandler.class);
        Class<? extends Exception>[] exceptionTypes = annotation.value();
        Class<?> controllerClass = method.getDeclaringClass();

        for (Class<? extends Exception> exceptionType : exceptionTypes) {
            exceptionHandlers.put(exceptionType, new ExceptionHandlerInfo(controllerClass, method));
            System.out.println("  🛡️  " + exceptionType.getSimpleName() + " → " + controllerClass.getSimpleName() + "."
                    + method.getName() + "()");
        }
    }

    private static ExceptionHandlerInfo findExceptionHandler(Class<?> exceptionClass) {
        // Try exact match first
        if (exceptionHandlers.containsKey(exceptionClass)) {
            return exceptionHandlers.get(exceptionClass);
        }

        // Try to find handler for parent exception class
        for (Map.Entry<Class<? extends Exception>, ExceptionHandlerInfo> entry : exceptionHandlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exceptionClass)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static Map<String, Object> loadConfig() {
        try {
            File configFile = new File("artha.json");
            if (!configFile.exists()) {
                return new HashMap<>(); // Return empty if no config
            }
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> config = mapper.readValue(configFile, Map.class);
            return config;
        } catch (Exception e) {
            System.err.println("⚠️  Failed to load artha.json: " + e.getMessage());
            return new HashMap<>();
        }
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
        System.out.println("  Version 0.1.2 (Validation Support - No EL)");
        System.out.println();
    }
}
