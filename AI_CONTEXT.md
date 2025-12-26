# ARTHA Framework - Comprehensive AI Developer Guide

**System Role**: You are an expert backend engineer specializing in the **ARTHA Framework**. ARTHA is a modern, lightweight Java framework designed for students and education, prioritizing "LeetCode-style" logical simplicity over enterprise boilerplate.

When generating code, you must strictly adhere to the patterns and APIs defined below. Do NOT use Spring annotations (like `@Autowired`, `@RequestMapping`) or standard Servlets.

---

## 1. Project Structure & Configuration

### Directory Layout
```text
project-root/
├── artha.json           # Main Configuration
├── src/
│   ├── models/          # POJOs
│   ├── controllers/     # @Step definitions
│   ├── services/        # Business Logic
│   └── middleware/      # @Before/@After interceptors
└── target/              # Build artifacts
```

### Configuration (`artha.json`)
This file controls the server, database connection, and dependencies.
```json
{
  "port": 8080,
  "database": {
    "driver": "mysql",         // Options: "mysql", "postgresql", "sqlite"
    "host": "localhost",
    "port": 3306,
    "name": "artha_social",
    "username": "root",
    "password": "password"
  },
  "dependencies": {
    "hikari": "5.1.0",         // Connection Pooling
    "jackson": "2.16.1"        // JSON Serialization
  }
}
```

---

## 2. Core Components

### A. Controllers & Routing (`@Step`)
*   **Annotations**: `@Step(path = "...", method = "...")`
*   **Class Level**: Defines a base path (optional).
*   **Method Level**: Defines the endpoint.
*   **Signatures**: Methods can take `Request`, `Response`, `Connection` (for manual DB), or POJOs (via `@Inject` or deserialization).

**Example:**
```java
@Step(path = "/api/v1")
public class UserController {

    // 1. GET with Path Param
    @Step(path = "/users/{id}", method = "GET")
    public Object getUser(Request req) {
        String id = req.param("id");
        return db.table("users").find(id, User.class);
    }

    // 2. POST with Body Parsing
    @Step(path = "/users", method = "POST")
    public Object createUser(Request req) {
        User user = req.body(User.class); // Auto-parse JSON
        db.table("users").save(user);     // Auto-save
        return user;
    }
}
```

### B. Dependency Injection (`@Inject`)
*   **Scope**: Singleton.
*   **Mechanism**: Field injection.
*   **Usage**: Inject `Database` or your own Services.

```java
public class UserController {
    @Inject
    private Database db;        // Built-in Singleton

    @Inject
    private UserService service; // Your Custom Service
}
```

---

## 3. Database & ORM (QueryBuilder)

ARTHA provides a fluent `QueryBuilder` via `db.table(...)`.

### Supported Methods
*   `find(id, Class<T>)`: Select by ID.
*   `save(Object entity)`: Smart Insert (if ID null) or Update (if ID set).
*   `insert(Map<String, Object>)`: Manual insert.
*   `update(Map<String, Object>)`: Manual update (requires `.where()`).
*   `delete()`: Delete (requires `.where()`).
*   `where(col, op, val)`: Add condition (e.g. `.where("age", ">", 18)`).
*   `orderBy(col, dir)`: Sort results.
*   `limit(n)`: Limit rows.
*   `get(Class<T>)`: Execute SELECT and return List of Objects.
*   `execute(sql, params...)`: Run raw SQL (Returns `List<Map<String, Object>>`).

### Model Mapping
Use Jackson annotations to map **CamelCase (Java)** to **SnakeCase (DB)**.

```java
public class User {
    private Integer id; // Must be Integer (nullable) for auto-increment detection

    @JsonProperty("full_name")  // Maps to DB column 'full_name'
    @JsonAlias("fullName")      // Maps from JSON field 'fullName'
    private String fullName;
}
```

---

## 4. Middleware (Security & Interceptors)

### The Interface
Implement `dev.artha.http.Middleware`.
```java
public class AuthMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) throws Exception {
        if (req.header("Authorization") == null) {
            res.status(401);
            throw new RuntimeException("Missing Token");
        }
    }
}
```

### Usage (`@Before` / `@After`)
Attach to a **Class** (all methods) or a single **Method**.
```java
@Step(path = "/admin")
@Before(AuthMiddleware.class) // Runs BEFORE method
public class AdminController { ... }
```

---

## 5. Other Features

### Scheduled Tasks (`@Scheduled`)
Run background jobs.
```java
@Scheduled(fixedRate = 60000) // Every 60 seconds
public void cleanup() {
    System.out.println("Cleaning cache...");
}
```

### Global Error Handling
Catch exceptions across the app.
```java
public class GlobalErrorHandler {
    @ExceptionHandler(RuntimeException.class)
    public Object handle(Exception e, Response res) {
        res.status(400);
        return Map.of("error", e.getMessage());
    }
}
```

---

## 6. Best Practices for AI Generation
1.  **Always** use `req.body(Class)` for POST requests; avoid manual Map parsing.
2.  **Always** use `db.table(...).save()` for persistence; avoid raw INSERT SQL.
3.  **Always** use `@Inject` for services; do not uses `new Service()`.
4.  **Always** return `Map` or POJOs; the framework auto-converts to JSON.
5.  **Prefer** `@Before` middleware for Auth checks over manual `if` statements.

**Objective**: Write clean, concise, and student-friendly code.
