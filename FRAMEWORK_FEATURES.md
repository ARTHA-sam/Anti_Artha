# ARTHA Framework - Complete Feature List

## ✅ Core Features Implemented

### 1. HTTP Layer (Request & Response)
**Location:** `dev.artha.http.*`

**Request Interface:**
- `query(String key)` - Get query parameter
- `query(String key, String defaultValue)` - Get with default
- `queryMap()` - All query parameters
- `path(String key)` - Path parameters from routes like `/users/:id`
- `body(Class<T>)` - Parse JSON request body
- `bodyAsString()` - Raw body content
- `header(String key)` - Get request header
- `headers()` - All headers as map
- `method()` - HTTP method (GET, POST, etc.)
- `url()` - Request URL
- `ip()` - Client IP address

**Response Interface:**
- `status(int code)` - Set HTTP status code
- `json(Object obj)` - Send JSON response
- `text(String text)` - Send text response
- `header(String key, String value)` - Set response header

### 2. HTTP Methods Supported
- ✅ GET
- ✅ POST
- ✅ PUT
- ✅ DELETE
- ✅ PATCH (newly added)

### 3. Error Handling
Returns standardized JSON error format:
```json
{
  "error": true,
  "message": "Error details here",
  "path": "/api/endpoint"
}
```

### 4. Validation
- Checks for `handle(Request, Response)` method
- Skips classes without the required method
- Prints warning: `⚠️ Missing handle() in ClassName — skipping`

### 5. Default Method Resolution
- `@Step` annotation defaults to GET
- No need to specify `method = "GET"` explicitly

### 6. Configuration System
**Location:** `dev.artha.config.ArthaConfig`

**Features:**
- Reads `artha.json` from project root
- Falls back to defaults if missing
- Exposes: `getPort()`, `getEnv()`, `getDependencies()`

**Example artha.json:**
```json
{
  "port": 8080,
  "env": "dev",
  "dependencies": ["postgresql", "lombok"]
}
```

### 7. Dependency Management
**Location:** `dev.artha.deps.DependencyLoader`

**Features:**
- Maps dependency names to Maven coordinates
- Downloads JARs from Maven Central
- Caches in `.artha/libs/` directory
- Dynamically adds to classpath at runtime

**Pre-configured Dependencies:**
- `lombok` → org.projectlombok:lombok:1.18.32
- `postgresql` → org.postgresql:postgresql:42.7.3
- `mysql-connector` → com.mysql:mysql-connector-j:8.3.0
- `gson` → com.google.code.gson:gson:2.10.1
- `jackson` → com.fasterxml.jackson.core:jackson-databind:2.16.1
- `sqlite` → org.xerial:sqlite-jdbc:3.45.1.0
- `h2` → com.h2database:h2:2.2.224
- `logback` → ch.qos.logback:logback-classic:1.4.14

### 8. Auto CORS
- Enabled by default
- Allows all origins
- No configuration required

### 9. Port Configuration
Multiple ways to set port:
1. `artha.json`: `"port": 8080`
2. System property: `-Dartha.port=8080`
3. Default: `8080`

Priority: System property > artha.json > default

### 10. Classpath Scanning
- Uses Reflections library
- Scans entire classpath for `@Step` annotations
- Automatically registers all annotated classes

## Architecture

### Package Structure
```
dev.artha/
├── annotations/
│   └── Step.java              - Route annotation
├── core/
│   └── Runtime.java           - Main server entry point
├── http/
│   ├── Request.java           - Request interface
│   ├── RequestImpl.java       - Javalin wrapper
│   ├── Response.java          - Response interface
│   └── ResponseImpl.java      - Javalin wrapper
├── config/
│   └── ArthaConfig.java       - Configuration loader
└── deps/
    └── DependencyLoader.java  - Dependency resolver
```

### Dependencies
**Runtime:**
- Javalin (HTTP server)
- Reflections (classpath scanning)
- Gson (JSON parsing for config and responses)

**Development:**
- No dependencies required
- Single `@Step` annotation needed

## Usage Examples

### Basic Endpoint
```java
@Step(path = "/hello")
public class Hello {
    public String handle(Request req, Response res) {
        return "Namaste from ARTHA!";
    }
}
```

### With Query Parameters
```java
@Step(path = "/greet")
public class Greet {
    public String handle(Request req, Response res) {
        String name = req.query("name", "World");
        return "Hello, " + name;
    }
}
```

### JSON Response
```java
@Step(path = "/products")
public class Products {
    public Object handle(Request req, Response res) {
        return Map.of(
            "products", List.of("Laptop", "Mouse"),
            "count", 2
        );
    }
}
```

### Path Parameters
```java
@Step(path = "/users/:id")
public class GetUser {
    public Object handle(Request req, Response res) {
        String id = req.path("id");
        return Map.of("userId", id, "name", "John");
    }
}
```

### PATCH Request
```java
@Step(path = "/users/:id", method = "PATCH")
public class UpdateUser {
    public Object handle(Request req, Response res) {
        String id = req.path("id");
        res.status(200);
        return Map.of("updated", true, "id", id);
    }
}
```

### POST with Body
```java
@Step(path = "/users", method = "POST")
public class CreateUser {
    public Object handle(Request req, Response res) {
        User user = req.body(User.class);
        res.status(201);
        return Map.of("created", true, "user", user);
    }
}
```

## Running the Application

### Standard Run
```bash
java -cp "build:runtime/target/classes" \
     dev.artha.core.Runtime
```

### With Port
```bash
java -Dartha.port=3000 \
     -cp "build:runtime/target/classes" \
     dev.artha.core.Runtime
```

### With Dependencies
```bash
java -cp "build:.artha/libs/*:runtime/target/classes" \
     dev.artha.core.Runtime
```

## Startup Output
```
   █████╗ ██████╗ ████████╗██╗  ██╗ █████╗ 
  ██╔══██╗██╔══██╗╚══██╔══╝██║  ██║██╔══██╗
  ███████║██████╔╝   ██║   ███████║███████║
  ██╔══██║██╔══██╗   ██║   ██╔══██║██╔══██║
  ██║  ██║██║  ██║   ██║   ██║  ██║██║  ██║
  ╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝

  Simple Java Backend Framework for Students
  Version 0.1.0

✓ Loaded artha.json
  Dependencies: [postgresql, lombok]

📦 Loading dependencies...
  ✓ Found postgresql-42.7.3.jar
  ✓ Found lombok-1.18.32.jar

ℹ️  Using port: 8080

🔍 Scanning for routes...
  GET  /hello → Hello
  POST /users → UserApi
  PATCH /users/:id → UpdateUser

✓ Server started at http://localhost:8080

Press Ctrl+C to stop
```

## Framework Philosophy

**Simple:** Single annotation, single method
**Fast:** Minimal overhead, direct routing
**Flexible:** Use any Java code, any database
**Student-Friendly:** Clear errors, easy debugging
**Production-Ready:** Proper error handling, CORS, config
