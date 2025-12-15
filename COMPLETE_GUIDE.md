# ARTHA Framework - Complete Guide

## 🎯 What is ARTHA?

ARTHA is a **Spring Boot-like framework** built from scratch to teach students how modern backend frameworks work. It has ALL the features you need to build production-ready APIs!

---

## 📋 All Annotations We Created

### 1. **@Step** - Routing
**File:** `runtime/src/main/java/dev/artha/annotations/Step.java`

**Purpose:** Define REST API endpoints (like Spring's `@GetMapping`)

**How it works:**
```java
@Step(path = "/api")  // Class-level: Base path
public class UserController {
    
    @Step(path = "/users", method = "GET")  // Method-level: Endpoint
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

**Behind the scenes:**
- Runtime scans for `@Step` annotations
- Registers routes with Javalin web server
- Maps HTTP methods to Java methods

---

### 2. **@Inject** - Dependency Injection
**File:** `runtime/src/main/java/dev/artha/annotations/Inject.java`

**Purpose:** Auto-inject dependencies (like Spring's `@Autowired`)

**How it works:**
```java
public class UserService {
    // This is your service
}

@Step(path = "/api")
public class UserController {
    
    @Inject  // Automatically creates UserService instance
    private UserService userService;
}
```

**Behind the scenes:**
- `DIContainer` is a singleton managing all instances
- When controller is created, DIContainer:
  1. Scans for `@Inject` fields
  2. Creates/retrieves service instance
  3. Injects it into the field
- **ALL instances are SINGLETONS** (one instance per class)

**DIContainer.java logic:**
```java
// Simplified version
public <T> T get(Class<T> type) {
    if (instances.containsKey(type)) {
        return instances.get(type);  // Return existing
    }
    T newInstance = type.newInstance();  // Create new
    injectDependencies(newInstance);     // Inject its deps
    instances.put(type, newInstance);    // Cache it
    return newInstance;
}
```

---

### 3. **@Before** & **@After** - Middleware
**Files:** 
- `runtime/src/main/java/dev/artha/annotations/Before.java`
- `runtime/src/main/java/dev/artha/annotations/After.java`

**Purpose:** Run code before/after request handling

**How it works:**
```java
public class AuthMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) throws Exception {
        String token = req.header("Authorization");
        if (token == null) {
            res.status(401);
            throw new UnauthorizedException();
        }
    }
}

@Step(path = "/api")
@Before({AuthMiddleware.class})  // Runs before all endpoints
public class SecureController {
    // All endpoints here are protected
}
```

**Behind the scenes:**
- Runtime checks for `@Before`/`@After` annotations
- Before handling request:
  1. Instantiates middleware classes
  2. Calls their `apply()` method
  3. If exception thrown, stops execution
- After handling request:
  1. Response already sent
  2. Runs `@After` middleware (logging, cleanup)

---

### 4. **@ExceptionHandler** - Error Handling
**File:** `runtime/src/main/java/dev/artha/annotations/ExceptionHandler.java`

**Purpose:** Centralized exception handling (like Spring's `@ExceptionHandler`)

**How it works:**
```java
public class NotFoundException extends Exception {
    public NotFoundException(String msg) { super(msg); }
}

@Step(path = "/api")
public class MyController {
    
    @Step(path = "/users/{id}")
    public User getUser(Request req) throws Exception {
        User user = service.findById(req.param("id"));
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }
    
    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFound(Exception e, Request req, Response res) {
        res.status(404);
        return Map.of("error", e.getMessage());
    }
}
```

**Behind the scenes:**
- Runtime scans for `@ExceptionHandler` methods
- Stores them in a `Map<Class<Exception>, HandlerInfo>`
- When exception thrown:
  1. Checks if handler exists for that exception type
  2. Supports inheritance (parent exception handlers work)
  3. Invokes handler method with (Exception, Request, Response)
  4. Returns handler's result as response

---

### 5. **@ConfigValue** - Configuration Injection
**File:** `runtime/src/main/java/dev/artha/annotations/ConfigValue.java`

**Purpose:** Inject config values from artha.json (like Spring's `@Value`)

**How it works:**

**artha.json:**
```json
{
  "app": {
    "name": "My App",
    "maxUsers": 1000
  }
}
```

**Java:**
```java
public class AppConfig {
    
    @ConfigValue("app.name")
    private String appName;  // Injected: "My App"
    
    @ConfigValue("app.maxUsers")
    private int maxUsers;  // Injected: 1000
}
```

**Behind the scenes:**
- `ConfigManager` loads artha.json at startup
- `DIContainer` scans for `@ConfigValue` fields
- Uses dot notation to navigate JSON (app.name → {"app": {"name": ...}})
- Converts value to field type (String, int, boolean, etc.)

**ConfigManager.java logic:**
```java
public Object get(String path) {
    String[] keys = path.split("\\.");  // "app.name" → ["app", "name"]
    Object current = config;
    for (String key : keys) {
        current = ((Map) current).get(key);  // Navigate nested
    }
    return current;
}
```

---

### 6. **@Scheduled** - Background Tasks
**File:** `runtime/src/main/java/dev/artha/annotations/Scheduled.java`

**Purpose:** Run methods periodically in background (like Spring's `@Scheduled`)

**How it works:**
```java
public class BackgroundJobs {
    
    @Scheduled(fixedRate = 60000)  // Every 60 seconds
    public void cleanupTask() {
        System.out.println("Cleaning up...");
    }
}
```

**Behind the scenes:**
- Runtime scans for `@Scheduled` methods
- `TaskScheduler` uses `ScheduledExecutorService` (Java's thread pool)
- For each method:
  1. Creates a Runnable that invokes the method
  2. Schedules it at fixed rate
  3. Runs in separate thread (non-blocking)

**TaskScheduler.java logic:**
```java
executor.scheduleAtFixedRate(() -> {
    method.invoke(instance);  // Call the method
}, 0, rateMillis, TimeUnit.MILLISECONDS);
```

---

### 7. **@Valid** - Validation
**File:** `runtime/src/main/java/dev/artha/annotations/Valid.java`

**Purpose:** Validate request bodies

**How it works:**
```java
public class User {
    @NotBlank
    private String name;
    
    @Email
    private String email;
}

@Step(path = "/users", method = "POST")
public User createUser(@Valid User user) {
    return userService.save(user);
}
```

**Behind the scenes:**
- Uses Hibernate Validator
- Before method execution, validates the object
- If validation fails, throws `ValidationException`

---

## 🛠️ Core Components

### DIContainer (Dependency Injection Container)
**File:** `runtime/src/main/java/dev/artha/core/DIContainer.java`

**What it does:**
- Manages ALL object instances (Singleton pattern)
- Creates objects when first requested
- Injects `@Inject` and `@ConfigValue` fields
- Ensures one instance per class

**Key methods:**
```java
T get(Class<T> type)           // Get/create instance
void injectDependencies(Object) // Inject fields
void registerInstance(Class, T) // Manually register
```

---

### ConfigManager
**File:** `runtime/src/main/java/dev/artha/core/ConfigManager.java`

**What it does:**
- Loads artha.json at startup
- Provides access to config values via dot notation
- Used by DIContainer for `@ConfigValue` injection

---

### TaskScheduler
**File:** `runtime/src/main/java/dev/artha/core/TaskScheduler.java`

**What it does:**
- Manages background scheduled tasks
- Uses Java's ScheduledExecutorService
- Runs tasks in separate threads

---

### QueryBuilder (ORM)
**File:** `runtime/src/main/java/dev/artha/db/QueryBuilder.java`

**What it does:**
- Fluent API for database operations
- **100% SQL injection safe** (uses PreparedStatement)
- Auto-maps results to POJOs

**How it works:**
```java
// SELECT
List<Product> products = db.table("products")
    .where("price", "<", 100)
    .orderBy("name")
    .limit(10)
    .get(Product.class);

// INSERT
int id = db.table("products")
    .insert(Map.of("name", "Laptop", "price", 999.99));

// UPDATE
db.table("products")
    .where("id", id)
    .update(Map.of("price", 899.99));

// DELETE
db.table("products")
    .where("id", id)
    .delete();
```

**Behind the scenes:**
- Builds SQL query with placeholders (?)
- Uses PreparedStatement to prevent injection
- Auto-maps ResultSet to Java objects using reflection

---

### Database
**File:** `runtime/src/main/java/dev/artha/db/Database.java`

**What it does:**
- Manages database connections (HikariCP pool)
- Supports SQLite, MySQL, PostgreSQL
- Provides `table()` method to get QueryBuilder

---

### Runtime (Main Entry Point)
**File:** `runtime/src/main/java/dev/artha/core/Runtime.java`

**What it does:**
1. Loads artha.json
2. Initializes ConfigManager
3. Initializes Database (if configured)
4. Scans for all annotations (`@Step`, `@Inject`, `@Scheduled`, etc.)
5. Registers routes with Javalin
6. Starts web server
7. Handles all requests

**Request flow:**
```
1. HTTP Request arrives
2. Runtime finds matching @Step method
3. Executes @Before middleware
4. Creates controller via DIContainer (auto-injects deps)
5. Invokes method
6. If exception → checks @ExceptionHandler
7. Executes @After middleware
8. Sends response
```

---

## 🎓 How Everything Works Together

### Example: Complete Request Flow

**artha.json:**
```json
{
  "port": 8080,
  "database": {"driver": "sqlite", "name": "app.db"},
  "app": {"name": "My API"}
}
```

**UserService.java:**
```java
public class UserService {
    @Inject
    private Database db;
    
    @ConfigValue("app.name")
    private String appName;
    
    public List<User> findAll() {
        return db.table("users").get(User.class);
    }
}
```

**LogMiddleware.java:**
```java
public class LogMiddleware implements Middleware {
    public void apply(Request req, Response res) {
        System.out.println("Request: " + req.path());
    }
}
```

**UserController.java:**
```java
@Step(path = "/api")
@Before({LogMiddleware.class})
public class UserController {
    
    @Inject
    private UserService userService;
    
    @Step(path = "/users", method = "GET")
    public List<User> getUsers() {
        return userService.findAll();
    }
    
    @ExceptionHandler(Exception.class)
    public Object handleError(Exception e, Response res) {
        res.status(500);
        return Map.of("error", e.getMessage());
    }
}
```

**BackgroundJob.java:**
```java
public class CleanupJob {
    @Scheduled(fixedRate = 3600000)  // Every hour
    public void cleanup() {
        System.out.println("Cleaning up old data...");
    }
}
```

**What happens when server starts:**

1. **Runtime.main()** starts
2. **Loads artha.json** → ConfigManager stores it
3. **Initializes Database** with SQLite config
4. **Scans annotations:**
   - Finds `@Step` on UserController
   - Finds `@Before` with LogMiddleware
   - Finds `@ExceptionHandler` 
   - Finds `@Scheduled` on CleanupJob
5. **Registers routes:** `GET /api/users` → UserController.getUsers()
6. **Schedules tasks:** CleanupJob.cleanup() every hour
7. **Starts server** on port 8080

**When GET /api/users request arrives:**

1. **Javalin** receives request
2. **Runtime** finds route handler
3. **Executes @Before:** LogMiddleware logs request
4. **Creates controller:**
   - DIContainer creates UserController
   - Sees `@Inject UserService`
   - DIContainer creates UserService
   - Sees `@Inject Database` in UserService
   - Injects Database instance
   - Sees `@ConfigValue("app.name")` in UserService
   - Injects "My API" from config
   - Injects UserService into controller
5. **Invokes method:** UserController.getUsers()
6. **UserService.findAll():**
   - Uses QueryBuilder to SELECT * FROM users
   - Auto-maps to User objects
   - Returns List<User>
7. **JSON serialization:** Jackson converts List to JSON
8. **Sends response:** 200 OK with JSON

**If error occurs:** `@ExceptionHandler` catches it and returns error JSON

---

## 🔧 Technologies Used

1. **Javalin** - Embedded web server (like Jetty in Spring Boot)
2. **Jackson** - JSON serialization/deserialization
3. **HikariCP** - Fast connection pooling
4. **Hibernate Validator** - Bean validation
5. **Reflections** - Annotation scanning
6. **SLF4J** - Logging
7. **JDBC Drivers** - SQLite, MySQL, PostgreSQL

---

## 🎯 Key Design Patterns

1. **Singleton Pattern** - DIContainer, ConfigManager, TaskScheduler
2. **Dependency Injection** - Inversion of Control
3. **Builder Pattern** - QueryBuilder fluent API
4. **Strategy Pattern** - Middleware interface
5. **Template Method** - Request handling flow
6. **Observer Pattern** - Scheduled tasks

---

## 📊 Summary

| Feature | Annotation | File | Purpose |
|---------|-----------|------|---------|
| Routing | `@Step` | Step.java | Define endpoints |
| DI | `@Inject` | Inject.java | Auto-inject deps |
| Middleware | `@Before/@After` | Before.java, After.java | Intercept requests |
| Exception | `@ExceptionHandler` | ExceptionHandler.java | Handle errors |
| Config | `@ConfigValue` | ConfigValue.java | Inject config |
| Scheduling | `@Scheduled` | Scheduled.java | Background tasks |
| Validation | `@Valid` | Valid.java | Validate data |

**You now have a COMPLETE, production-ready framework!** 🚀

Ready to build a real project to test everything? 💪
