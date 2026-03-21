# ARTHA Framework — Complete Knowledge Base

> **For the builder who wants to own what they built.**
> This document gives you everything you need to understand, explain, and confidently talk about this project end-to-end.

> **Note on naming:** The repository is called `Anti_Artha` — the framework itself is called **ARTHA**. The `Anti_Artha` name refers to the repo/organization that hosts it (ARTHA-sam/Anti_Artha), not the framework's name.

---

## Table of Contents

1. [What Is This Project?](#1-what-is-this-project)
2. [Why Was It Built?](#2-why-was-it-built)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Repository Structure Explained](#4-repository-structure-explained)
5. [The Runtime (Core Framework)](#5-the-runtime-core-framework)
   - [Annotations](#51-annotations-the-vocabulary)
   - [DIContainer — Dependency Injection](#52-dicontainer--dependency-injection)
   - [Runtime.java — The Server Brain](#53-runtimejava--the-server-brain)
   - [Database & QueryBuilder](#54-database--querybuilder)
   - [Request & Response](#55-request--response)
   - [Middleware](#56-middleware)
   - [TaskScheduler](#57-taskscheduler)
   - [ConfigManager](#58-configmanager)
6. [The CLI Tool](#6-the-cli-tool)
7. [The Social Network Example App](#7-the-social-network-example-app)
8. [The Frontend](#8-the-frontend)
9. [Tech Stack & Dependencies](#9-tech-stack--dependencies)
10. [How to Run the Project](#10-how-to-run-the-project)
11. [How Everything Connects — Full Flow](#11-how-everything-connects--full-flow)
12. [Key Design Decisions Explained](#12-key-design-decisions-explained)
13. [Common Interview / Presentation Questions & Answers](#13-common-interview--presentation-questions--answers)

---

## 1. What Is This Project?

**ARTHA** is a **custom-built Java web framework** — similar to Spring Boot — but designed to be **simple, educational, and student-friendly**.

- Full name: **ARTHA Framework** (the repo is called `Anti_Artha`)
- It lets developers build REST APIs using **simple Java files** without any XML, heavy configuration, or enterprise boilerplate
- The user writes plain Java classes annotated with custom annotations like `@Step`, `@Inject`, `@Before` — and the framework handles all the server, routing, and database wiring automatically

**Analogy**: Think of it like Spring Boot, but stripped down to only what students actually need, explained in plain terms.

---

## 2. Why Was It Built?

Spring Boot is powerful but intimidating for beginners — too much "magic," too many concepts. ARTHA was built to:

1. **Teach how frameworks work under the hood** — routing, dependency injection, middleware, ORM
2. **Give students a framework they actually understand** — they can read every line of source code
3. **Mirror the feel of modern frameworks** — Spring Boot-style annotations, Laravel-style query builder, Express.js-style middleware
4. **Reduce setup friction** — one `artha.json` config file, one command to start (`artha dev`)

---

## 3. High-Level Architecture

```
+----------------------------------------------------------+
|                    ARTHA Framework                        |
+----------------------------------------------------------+
|                                                           |
|   CLI Tool (Node.js)          User Project (Java)         |
|   ─────────────────           ─────────────────           |
|   artha dev  ──────────────►  Compiles .java files        |
|   artha new                   with artha-runtime.jar      |
|   artha add                                               |
|              │                                            |
|              ▼                                            |
|   artha-runtime.jar (Java)                                |
|   ─────────────────────────                               |
|   ┌─────────────┐  ┌──────────────────────┐              |
|   │  Runtime.java│  │ DIContainer           │              |
|   │  (main())   │  │ (Dependency Injection) │              |
|   └──────┬──────┘  └──────────────────────┘              |
|          │                                                |
|          ▼                                                |
|   ┌─────────────────────────────────────┐                |
|   │         Javalin HTTP Server          │                |
|   │  Routes scanned from @Step classes   │                |
|   │  Middleware from @Before/@After      │                |
|   └────────────────────┬────────────────┘                |
|                        │                                  |
|          ┌─────────────┼──────────────┐                  |
|          ▼             ▼              ▼                   |
|   ┌────────────┐ ┌──────────┐ ┌────────────────┐        |
|   │  Database  │ │Scheduler │ │ Config Manager  │        |
|   │ HikariCP   │ │ (cron)   │ │  (artha.json)   │        |
|   │ QueryBuilder│ └──────────┘ └────────────────┘        |
|   └────────────┘                                         |
|                                                           |
+----------------------------------------------------------+
```

### Request Flow (what happens when an API call arrives)

```
HTTP Request (e.g. POST /api/auth/register)
        │
        ▼
  Javalin HTTP Server (embedded, port 8080)
        │
        ▼
  Middleware chain (@Before)
  └─ AuthMiddleware.apply() — checks Authorization header
        │
        ▼
  Controller method (AuthController.register())
  └─ DIContainer injects @Inject fields (Database db)
  └─ ConfigManager injects @ConfigValue fields
  └─ req.body(User.class) parses JSON body
  └─ db.table("users").save(user) runs SQL via HikariCP
        │
        ▼
  Return value serialized to JSON (Jackson)
        │
        ▼
  HTTP Response (200/201/400/401/500)
```

---

## 4. Repository Structure Explained

```
Anti_Artha/
├── runtime/                    ← THE FRAMEWORK ITSELF (Java/Maven)
│   ├── pom.xml                 ← Maven build config, all dependencies
│   ├── src/main/java/dev/artha/
│   │   ├── annotations/        ← Custom annotations (@Step, @Inject, etc.)
│   │   ├── core/               ← Main engine: Runtime, DIContainer, Scheduler
│   │   ├── db/                 ← Database and QueryBuilder
│   │   ├── http/               ← Request, Response, Middleware interfaces
│   │   ├── config/             ← ArthaConfig model
│   │   └── deps/               ← DependencyLoader (downloads JARs)
│   └── target/                 ← Build output: artha-runtime-0.1.0.jar
│
├── cli/                        ← THE COMMAND LINE TOOL (Node.js/npm)
│   ├── bin/artha.js             ← Entry point: `artha` command
│   ├── src/commands/
│   │   ├── dev.js              ← `artha dev` (compile + watch + run)
│   │   ├── new.js              ← `artha new <project>` (scaffold)
│   │   ├── add.js              ← `artha add <dependency>`
│   │   └── setup.js            ← `artha setup` (VS Code config)
│   └── lib/
│       ├── artha-runtime.jar   ← Pre-built framework JAR (bundled)
│       └── mysql-connector-j-8.2.0.jar
│
├── examples/
│   ├── social-network/         ← MAIN DEMO APP: Twitter-like social network
│   │   ├── src/                ← User's Java source files
│   │   │   ├── User.java       ← Model/POJO
│   │   │   ├── Post.java       ← Model/POJO
│   │   │   ├── AuthController.java   ← Register/Login endpoints
│   │   │   ├── PostController.java   ← Post CRUD + feed + likes
│   │   │   ├── AuthMiddleware.java   ← Token verification
│   │   │   └── CleanupTask.java      ← Scheduled background job
│   │   ├── frontend/           ← Simple HTML/CSS/JS UI
│   │   ├── database.sql        ← MySQL schema (5 tables)
│   │   └── artha.json          ← App config (port, DB, settings)
│   │
│   ├── comprehensive-demo/     ← Feature showcase demo
│   └── v2-demo/                ← v2 API demo
│
├── docs/                       ← Website/documentation (HTML)
│
├── README.md                   ← Main project readme
├── AI_CONTEXT.md               ← Guide for AI to generate ARTHA code
├── KNOWLEDGE_BASE.md           ← THIS FILE
└── *.md                        ← Various feature/guide docs
```

---

## 5. The Runtime (Core Framework)

The `runtime/` folder is the heart of ARTHA. It compiles into `artha-runtime-0.1.0.jar` — a single file that contains the entire framework.

### 5.1 Annotations — The Vocabulary

These are Java annotations you put on your classes/methods to tell ARTHA what to do.

| Annotation | Where Used | What It Does |
|---|---|---|
| `@Step(path, method)` | Class + Method | Marks a class/method as an HTTP route handler |
| `@Inject` | Field | Auto-injects a dependency (singleton) |
| `@Before(Class[])` | Class + Method | Runs middleware BEFORE the handler |
| `@After(Class[])` | Class + Method | Runs middleware AFTER the handler |
| `@ExceptionHandler(ExClass)` | Method | Catches a specific exception thrown anywhere |
| `@ConfigValue("key")` | Field | Injects a value from `artha.json` |
| `@Scheduled(fixedRate)` | Method | Runs method repeatedly at given interval (ms) |
| `@Valid` | Parameter | Triggers bean validation on a parameter |
| `@Body` | Parameter | Marks a parameter as the request body |
| `@PathParam` | Parameter | Binds a path variable |
| `@Query` | Parameter | Binds a query string parameter |
| `@Status(code)` | Method | Sets default HTTP status for a response |
| `@RestController` | Class | Alternative to `@Step` for v2 REST controllers |

**Example of @Step routing:**
```java
@Step(path = "/api/users")          // base path for all methods in this class
public class UserController {

    @Step(path = "/{id}", method = "GET")   // becomes GET /api/users/{id}
    public Object getUser(Request req) {
        String id = req.param("id");   // extracts {id} from URL
        return db.table("users").find(id, User.class);
    }
}
```

---

### 5.2 DIContainer — Dependency Injection

**File:** `runtime/src/main/java/dev/artha/core/DIContainer.java`

This is the "wiring" system. Instead of you creating objects with `new`, ARTHA creates them and connects them automatically.

**How it works:**
1. It's a **Singleton** — one container exists for the whole app
2. Uses a `ConcurrentHashMap<Class<?>, Object>` to store one instance per class
3. When you put `@Inject` on a field, DIContainer:
   - Checks if an instance of that type already exists
   - If yes → returns it (singleton reuse)
   - If no → creates a new instance using reflection, then injects its dependencies too (recursive)
4. **Special case for `Database`**: Always retrieves via `Database.getInstance()` to guarantee the initialized singleton

**What gets injected:**
- `@Inject` fields → calls `DIContainer.get(fieldType)` → returns/creates singleton
- `@ConfigValue("key")` fields → calls `ConfigManager.get("key")` → injects the value from `artha.json`

```java
public class PostController {
    @Inject
    private Database db;            // DIContainer creates Database singleton, injects here

    @ConfigValue("app.maxPostLength")
    private int maxPostLength;      // ConfigManager reads artha.json → "app.maxPostLength" → 500
}
```

---

### 5.3 Runtime.java — The Server Brain

**File:** `runtime/src/main/java/dev/artha/core/Runtime.java`

This is `main()` — the entry point of every ARTHA application. When you run `artha dev`, this class starts up.

**Startup sequence:**
```
1. Print ASCII banner
2. Load artha.json → ConfigManager
3. Initialize Database (HikariCP connection pool)
4. Register Database with DIContainer
5. Create Javalin HTTP server (with Jackson JSON + CORS enabled)
6. Use Reflections library to scan all classes on the classpath for:
   - @Step annotations on classes   → registerClassRoute()
   - @Step annotations on methods   → registerMethodRoute()
   - @RestController annotations    → registerRestController()
   - @ExceptionHandler annotations  → registerExceptionHandler()
   - @Scheduled annotations         → registerScheduledTask()
7. Start Javalin on configured port (default 8080)
```

**Route registration:**
- Finds all `@Step`-annotated classes and methods via the `reflections` library
- Combines class-level and method-level paths: `/api/users` + `/{id}` = `/api/users/{id}`
- Registers Javalin handlers: `app.get(path, handler)`, `app.post(path, handler)`, etc.

**Request handling (`handleRequest`):**
1. Create `RequestImpl` and `ResponseImpl` wrappers around the Javalin context
2. Get the controller instance from DIContainer (triggers `@Inject` and `@ConfigValue`)
3. Run `@Before` middleware chain
4. Resolve method parameters (inject `Request`, `Response`, `Connection`, or parsed body)
5. Invoke the controller method
6. Serialize return value to JSON
7. Run `@After` middleware chain
8. Handle any exceptions through registered `@ExceptionHandler` or default 500 response

---

### 5.4 Database & QueryBuilder

**Files:**
- `runtime/src/main/java/dev/artha/db/Database.java`
- `runtime/src/main/java/dev/artha/db/QueryBuilder.java`

#### Database.java

- Singleton class wrapping a **HikariCP** connection pool
- Initialized once from `artha.json` database config
- Supports: **MySQL**, **PostgreSQL**, **SQLite**
- Key methods:
  - `initialize(config)` — sets up JDBC URL + HikariCP pool
  - `getConnection()` — borrows a connection from the pool
  - `table("tableName")` — creates a QueryBuilder for that table
  - `execute(sql, params...)` — runs raw SQL with prepared statement safety

#### QueryBuilder.java — Fluent API

The QueryBuilder lets you build SQL queries in a readable, safe way using **method chaining**:

```java
// SELECT * FROM users WHERE age > 18 ORDER BY name ASC LIMIT 10
List<User> users = db.table("users")
    .where("age", ">", 18)
    .orderBy("name", "ASC")
    .limit(10)
    .get(User.class);

// INSERT INTO products (...) VALUES (...)
int newId = db.table("products")
    .insert(Map.of("name", "Laptop", "price", 999.99));

// UPDATE products SET price = 899.99 WHERE id = 5
db.table("products")
    .where("id", 5)
    .update(Map.of("price", 899.99));

// DELETE FROM products WHERE id = 5
db.table("products").where("id", 5).delete();

// Smart save: INSERT if id is null/0, UPDATE if id exists
db.table("users").save(userObject);  // Uses Jackson to convert object → Map
```

**All queries use `PreparedStatement`** — parameters are never concatenated into SQL strings, making it 100% SQL-injection safe.

**How `save()` works:**
1. Converts the Java object to a `Map<String, Object>` using Jackson
2. Checks if the `id` field is null or 0
3. If no ID → calls `insert()` (auto-increment by DB)
4. If ID exists → calls `update()` with `WHERE id = ?`

**How `get(Class<T>)` works:**
1. Builds and executes SELECT SQL
2. Reads ResultSet rows into `Map<String, Object>`
3. Uses Jackson `ObjectMapper.convertValue(map, clazz)` to convert each map into the target class

---

### 5.5 Request & Response

**Files:**
- `runtime/src/main/java/dev/artha/http/Request.java` (interface)
- `runtime/src/main/java/dev/artha/http/RequestImpl.java` (implementation)
- `runtime/src/main/java/dev/artha/http/Response.java` (interface)
- `runtime/src/main/java/dev/artha/http/ResponseImpl.java` (implementation)

These are thin wrappers around Javalin's `Context` object that give a cleaner API.

**Request methods:**
```java
req.query("name")             // GET /api?name=value  →  "value"
req.query("page", "1")        // with default value
req.param("id")               // GET /api/users/{id}  →  "123"
req.body(User.class)          // Parse JSON body as User object
req.bodyAsMap()               // Parse JSON body as Map<String, Object>
req.bodyAsString()            // Raw body string
req.header("Authorization")   // Read header
req.method()                  // "GET", "POST", etc.
req.path()                    // "/api/users/123"
req.ip()                      // "192.168.1.1"
```

**Response methods:**
```java
res.status(201)               // Set HTTP status code
res.status(404)
res.header("X-Custom", "value")  // Set response header
```

---

### 5.6 Middleware

**File:** `runtime/src/main/java/dev/artha/http/Middleware.java`

```java
public interface Middleware {
    void apply(Request req, Response res) throws Exception;
}
```

To create middleware, implement this interface:

```java
public class AuthMiddleware implements Middleware {
    @Override
    public void apply(Request req, Response res) throws Exception {
        String auth = req.header("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            res.status(401);
            throw new Exception("Unauthorized");  // stops the chain
        }
        // If no exception is thrown, the request proceeds to the controller
    }
}
```

Apply it with `@Before`:
```java
@Step(path = "/api/posts")
@Before({ AuthMiddleware.class })   // Runs before EVERY method in this class
public class PostController { ... }

// Or on a single method:
@Step(path = "/me", method = "GET")
@Before(AuthMiddleware.class)
public Object getMe(Request req) { ... }
```

---

### 5.7 TaskScheduler

**File:** `runtime/src/main/java/dev/artha/core/TaskScheduler.java`

Runs background jobs at fixed intervals using Java's `ScheduledExecutorService` (thread pool of 5).

```java
public class CleanupTask {
    @Scheduled(fixedRate = 60000)  // every 60 seconds (60000 ms)
    public void cleanExpiredSessions() {
        System.out.println("Cleaning up expired sessions...");
        // Background job logic here
    }
}
```

The `Runtime` scans for `@Scheduled` methods on startup and registers them with `TaskScheduler.scheduleFixedRate()`. Methods must be `void` with no parameters.

---

### 5.8 ConfigManager

**File:** `runtime/src/main/java/dev/artha/core/ConfigManager.java`

Reads `artha.json` and makes values accessible via dot-notation keys:

```json
{
  "app": {
    "name": "ARTHA Social",
    "maxPostLength": 500
  }
}
```

```java
ConfigManager.getInstance().get("app.name")        // → "ARTHA Social"
ConfigManager.getInstance().get("app.maxPostLength") // → 500
```

`DIContainer` calls `ConfigManager` automatically when it sees `@ConfigValue("key")` on a field.

---

## 6. The CLI Tool

**Location:** `cli/` folder  
**Language:** Node.js  
**Entry point:** `cli/bin/artha.js`  
**Published as:** npm package (run as `artha` command)

The CLI bridges the gap between the user's Java project and the Java runtime. It handles compilation, file watching, and hot reload.

### Commands

| Command | What It Does |
|---|---|
| `artha dev` | Compile Java files + start server + watch for changes (hot reload) |
| `artha new <name>` | Scaffold a new ARTHA project with template files |
| `artha add <package>` | Add a dependency (downloads JAR to `lib/`) |
| `artha build` | Build production JAR |
| `artha setup` | Configure VS Code settings for the project |

### How `artha dev` Works (Step by Step)

1. Reads `artha.json` from current directory
2. Determines port (CLI flag → artha.json → default 8080)
3. Calls `DependencyManager.install()` to download required JARs (listed in `artha.json dependencies`)
4. Finds the `artha-runtime.jar` (bundled in `cli/lib/`)
5. Runs `javac` to compile all `.java` files in `src/` into `build/`
6. Runs `java dev.artha.core.Runtime` with the classpath including the runtime JAR, user's `build/` folder, and dependency JARs
7. Sets up **file watcher** (via `chokidar`) on `src/**/*.java`
8. On file change → kills old Java process → recompiles → restarts

### Key Technical Detail

The CLI passes `--add-opens` JVM flags to `javac` for compatibility with newer JDK versions (21+), enabling annotation processing tools like Lombok to work with modules that are otherwise closed.

---

## 7. The Social Network Example App

**Location:** `examples/social-network/`

This is the main demo application — a **Twitter/Instagram-like social network** with:
- User registration and login
- Post creation and feed
- Like system
- Follow/follower system
- Comment system

### Database Schema (5 Tables)

```
users       — id, username, email, password, full_name, bio, profile_pic, timestamps
posts       — id, user_id, content, image_url, likes_count, comments_count, timestamps  
likes       — id, user_id, post_id, created_at (unique constraint: 1 like per user per post)
follows     — id, follower_id, following_id, created_at (unique: can't follow same person twice)
comments    — id, user_id, post_id, content, timestamps
```

### Java Source Files

#### `User.java` — The Model
Plain Java object (POJO) with Jackson annotations for DB column name mapping:
```java
public class User {
    private Integer id;            // Integer (not int) so null means "new record"
    private String username;
    private String email;
    private String password;
    
    @JsonProperty("full_name")     // DB column is "full_name", Java field is "fullName"
    @JsonAlias("fullName")         // Accepts "fullName" in JSON input too
    private String fullName;
    private String bio;
    // getters + setters...
}
```

#### `Post.java` — The Model
Similar POJO for posts, includes `username` and `fullName` for JOIN results.

#### `AuthController.java` — Auth Endpoints

| Endpoint | Method | What It Does |
|---|---|---|
| `/api/auth/register` | POST | Creates new user, returns token |
| `/api/auth/login` | POST | Verifies credentials, returns token |
| `/api/auth/me` | GET | Returns authenticated user info (protected by `@Before(AuthMiddleware.class)`) |

Token format is simple: `"user_{id}_token"` (not JWT, by design for simplicity).

> ⚠️ **Security Warning:** This token format is intentionally insecure for educational purposes. Anyone who knows a user's ID can forge a valid token (e.g. `user_1_token`). There is no signature, no expiry, and no server-side validation of token authenticity. **Never use this approach in a real application.** Production apps must use cryptographically signed tokens (JWT) or server-side sessions.

#### `PostController.java` — Post Endpoints

| Endpoint | Method | What It Does |
|---|---|---|
| `/api/posts` | POST | Create a new post (auth required) |
| `/api/posts/feed` | GET | Get posts from followed users + own posts |
| `/api/posts/{id}/like` | POST | Like/unlike a post |

Uses `@Before({ AuthMiddleware.class })` on the class — ALL endpoints require authentication.
Uses `@ConfigValue("app.maxPostLength")` to enforce character limit.

#### `AuthMiddleware.java` — Token Guard
Checks `Authorization: Bearer user_1_token` header. If missing or invalid format → 401 Unauthorized.

> ⚠️ **Security Gap:** This middleware only checks that the token *looks like* the expected pattern — it does **not** verify that the token belongs to a real user, check an expiry date, or validate a cryptographic signature. Anyone can craft a `user_999_token` string and the middleware will accept it as valid. In a real application, middleware should either verify a JWT signature (using a secret key) or look up the token in a database/session store.

#### `CleanupTask.java` — Background Job
Runs periodically using `@Scheduled` to perform cleanup operations.

### Configuration (`artha.json`)
```json
{
    "port": 8080,
    "database": {
        "driver": "mysql",
        "host": "localhost",
        "port": 3306,
        "name": "artha_social",
        "username": "root",
        "password": "..."
    },
    "app": {
        "name": "ARTHA Social Network",
        "maxPostLength": 500
    }
}
```

---

## 8. The Frontend

**Location:** `examples/social-network/frontend/`  
**Files:** `index.html`, `style.css`, `app.js`

A simple, single-page web app with:
- **Login / Register tabs** — forms that call `/api/auth/login` and `/api/auth/register`
- **Feed page** — shows posts from the user's timeline
- **Create post** — textarea with 500 char limit, calls `POST /api/posts`
- **Like button** — calls `POST /api/posts/{id}/like`

The JS communicates with the ARTHA backend via `fetch()` API calls, sends the token as `Authorization: Bearer <token>` header.

---

## 9. Tech Stack & Dependencies

### Runtime (Java)

| Library | Version | Purpose |
|---|---|---|
| **Javalin** | 6.0.0 | Embedded HTTP server (lightweight, no servlet container needed) |
| **Reflections** | 0.10.2 | Scan classpath for `@Step`, `@Inject` etc. at startup |
| **Jackson Databind** | 2.16.1 | JSON parsing and serialization |
| **HikariCP** | 5.1.0 | High-performance JDBC connection pooling |
| **Hibernate Validator** | 8.0.1 | Bean validation (`@NotBlank`, `@Email`, `@Min`, etc.) |
| **Jakarta Validation API** | 3.0.2 | Validation annotation standards |
| **SLF4J Simple** | 2.0.9 | Logging (minimal) |
| **Javalin OpenAPI plugins** | 6.1.0 | Swagger/ReDoc API documentation |
| **Java 11+** | — | Minimum Java version required |
| **Maven** | — | Build tool, dependency management |

### CLI (Node.js)

| Library | Purpose |
|---|---|
| **commander** | Parses CLI commands and options |
| **chalk** | Colored terminal output |
| **ora** | Spinner animations ("Compiling...") |
| **chokidar** | File system watcher (hot reload) |
| **fs-extra** | Enhanced file system utilities |
| **axios** | HTTP client (for downloading dependencies) |

---

## 10. How to Run the Project

### Prerequisites
- Java 11+ installed (`java -version`)
- Node.js 16+ installed (`node -v`)
- MySQL running locally (for social-network example)

### Step 1: Set up the database
```sql
-- Run in MySQL Workbench or any MySQL client
source examples/social-network/database.sql
```

### Step 2: Configure database connection
Edit `examples/social-network/artha.json`:
```json
{
    "database": {
        "driver": "mysql",
        "host": "localhost",
        "port": 3306,
        "name": "artha_social",
        "username": "root",
        "password": "YOUR_PASSWORD"
    }
}
```

### Step 3: Start the server
```bash
cd examples/social-network

# Option A: Using CLI tool
npx artha dev

# Option B: Manual (if you built the runtime yourself)
javac -cp lib/artha-runtime.jar -d build src/*.java
java -cp "lib/artha-runtime.jar:build:lib/mysql-connector-j-8.2.0.jar" dev.artha.core.Runtime
```

### Step 4: Open the frontend
Open `examples/social-network/frontend/index.html` in a browser.

### Building the runtime from source
```bash
cd runtime
mvn clean package
# Output: runtime/target/artha-runtime-0.1.0.jar
```

---

## 11. How Everything Connects — Full Flow

Let's trace a complete request: **"User registers on the social network"**

### 1. Frontend sends request
```javascript
// frontend/app.js
fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        username: 'john_doe',
        email: 'john@example.com',
        fullName: 'John Doe',
        password: 'mypassword'
    })
})
```

### 2. Javalin receives the request
The HTTP server is running on port 8080. It matches `POST /api/auth/register` to the handler registered at startup.

### 3. Middleware check
No `@Before` middleware on the `register` endpoint, so it passes through immediately.

### 4. Controller method invoked
```java
// AuthController.java
@Step(path = "/register", method = "POST")
public Object register(Request req, Response res) {
    User user = req.body(User.class);
    // ↑ Jackson parses the JSON body into a User object
    // "fullName" in JSON → maps to fullName field (via @JsonAlias)
    // "full_name" in DB  → maps to fullName field (via @JsonProperty)
```

### 5. Database interaction
```java
    // Check for duplicate user
    List<Map<String, Object>> existing = db.execute(
        "SELECT id FROM users WHERE username = ? OR email = ?",
        user.getUsername(), user.getEmail()
    );
    // ↑ HikariCP borrows a connection, runs PreparedStatement, returns rows

    if (!existing.isEmpty()) {
        res.status(400);
        return Map.of("error", "Username or email already exists");
    }

    // Save to database using ORM
    int userId = db.table("users").save(user);
    // ↑ QueryBuilder:
    //   1. Jackson converts User object → Map
    //   2. id is null → INSERT
    //   3. Runs: INSERT INTO users (username, email, password, full_name, bio) VALUES (?, ?, ?, ?, ?)
    //   4. Returns auto-generated ID
```

### 6. Response
```java
    String token = "user_" + userId + "_token";
    res.status(201);
    return Map.of("token", token, "userId", userId, "username", user.getUsername());
    // ↑ Jackson serializes Map to JSON: {"token":"user_5_token","userId":5,"username":"john_doe"}
```

### 7. Client receives
```json
HTTP/1.1 201 Created
Content-Type: application/json

{"token":"user_5_token","userId":5,"username":"john_doe"}
```

---

## 12. Key Design Decisions Explained

### Why Java annotations?
Annotations are the standard way Java frameworks configure behavior declaratively (without code). Spring Boot pioneered this. ARTHA follows the same philosophy but with fewer, simpler annotations.

### Why Javalin instead of raw Servlets?
Javalin is a micro-framework on top of Jetty that provides clean routing with a simple API. It avoided having to write raw Servlet/Filter code, which is verbose and low-level.

### Why HikariCP?
HikariCP is the fastest JDBC connection pool available. Creating a new database connection per request is very slow. The pool pre-creates connections and reuses them — essential for any production-ready app.

### Why Jackson for JSON?
Jackson is the industry standard for Java JSON. It handles `camelCase ↔ snake_case` mapping automatically and can convert any Java object to/from JSON.

### Why Reflections library?
The `org.reflections` library lets you scan the classpath for classes/methods with specific annotations at runtime. This is how `Runtime.java` finds all your `@Step` controllers without you explicitly registering them — just like Spring Boot's component scan.

### Why Node.js for the CLI?
Node.js + npm is the most widely used CLI ecosystem. Tools like `chalk`, `ora`, `chokidar` (file watching), and `commander` provide a polished developer experience that would require much more work to build in Java.

### Why singleton scope for DI?
Singletons are simpler than prototype/request scope and appropriate for stateless services. Each controller and service is stateless (no instance variables that change per request), so sharing one instance is safe.

### Why simple tokens instead of JWT?
The social network example uses simple `user_{id}_token` format tokens deliberately — to keep the auth code readable for students. In a real app you'd use JWT or OAuth.

> ⚠️ **Critical Security Note (for learning purposes):** The current token system has three major vulnerabilities:
> - **Token forgery**: Anyone who knows a user's ID can construct a valid token without any credentials
> - **No expiration**: Tokens never expire — a leaked token is valid forever
> - **No signature**: Tokens are not cryptographically signed, so they cannot be verified as authentic
>
> In production, use **JWT (JSON Web Tokens)** — tokens are signed with a secret key using HMAC-SHA256, contain an expiry claim, and can be verified without a database lookup.

---

## 13. Common Interview / Presentation Questions & Answers

**Q: What did you build?**  
A: I built ARTHA — a custom Java web framework inspired by Spring Boot and Laravel. It lets developers build REST APIs using simple annotations like `@Step` for routing and `@Inject` for dependency injection. I also built a Node.js CLI tool and a social network demo app to prove the framework works end-to-end.

**Q: What problem does it solve?**  
A: Spring Boot is too complex for students to understand. ARTHA has the same developer experience but with source code you can read and understand completely. It teaches how routing, DI, middleware, and ORMs actually work under the hood.

**Q: How does routing work?**  
A: At startup, the `Runtime.java` uses the Reflections library to scan the entire classpath for classes/methods annotated with `@Step`. For each one, it extracts the path and HTTP method, then registers a Javalin handler. When a request comes in, Javalin matches the URL pattern and invokes the registered handler.

**Q: How does dependency injection work?**  
A: `DIContainer` is a singleton that holds a map of `Class → instance`. When it creates a controller, it uses Java reflection to inspect all fields. If a field has `@Inject`, it calls `DIContainer.get(fieldType)` to get or create the dependency. If it has `@ConfigValue`, it reads the value from the JSON config. This creates a tree of singletons automatically.

**Q: How do you prevent SQL injection?**  
A: Every query in the QueryBuilder uses `PreparedStatement`. Parameters are passed as `?` placeholders and set via `stmt.setObject(index, value)` — Java's JDBC layer ensures they're properly escaped. Raw string concatenation into SQL is never used.

**Q: How does hot reload work in the CLI?**  
A: The `artha dev` command uses `chokidar` to watch `src/**/*.java` for file changes. When a change is detected, it kills the running Java process, runs `javac` to recompile, and starts a new Java process. The total cycle is typically 1-3 seconds.

**Q: What's the difference between @Step on a class vs a method?**  
A: On a class, `@Step(path = "/api/users")` defines the base path prefix. On a method, `@Step(path = "/{id}", method = "GET")` defines the specific endpoint. The runtime combines them: `/api/users/{id}` with `GET`. A class-level `@Step` can also be used alone (legacy mode) with a single handler method named `handle()`.

**Q: How does middleware work?**  
A: Middleware classes implement the `Middleware` interface with one method: `apply(Request, Response)`. They're applied via `@Before(MiddlewareClass.class)` on a controller class or method. The runtime runs them in order before invoking the controller method. If the middleware throws an exception, the chain stops and the error is returned to the client.

**Q: What databases are supported?**  
A: MySQL, PostgreSQL, and SQLite. The `Database.java` builds the JDBC URL based on the `driver` field in `artha.json` and connects using HikariCP. For SQLite you only need the file name; for MySQL/PostgreSQL you need host, port, username, and password.

**Q: What is the social network demo?**  
A: It's a full-featured backend for a Twitter-like app. It has user registration/login, post creation, a feed that shows posts from people you follow, a like system, a follow system, and comments. The database has 5 tables in MySQL, and there's a simple HTML/CSS/JS frontend that calls the ARTHA backend.

**Q: What tech stack did you use?**  
A: The framework is Java 11 with Maven, built on top of Javalin (HTTP server), HikariCP (database pool), Jackson (JSON), and Hibernate Validator (bean validation). The CLI is Node.js with npm packages like commander, chalk, ora, and chokidar. The demo uses MySQL.

**Q: If you were to improve it, what would you change?**  
A: A few things: (1) Add proper JWT authentication — the current token (`user_{id}_token`) is easily forgeable and has no expiry or signature; JWT would fix all three issues. (2) Add database migration support (like Flyway). (3) Add a more complete ORM with relationship support (one-to-many, many-to-many). (4) Add an NPM package so it can be installed globally with `npm install -g artha`. (5) Add proper unit test infrastructure.
