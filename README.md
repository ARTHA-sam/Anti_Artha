# ARTHA Framework

**A Modern, Lightweight Java Web Framework for Education**

ARTHA is a Spring Boot-inspired web framework designed to teach students how modern backend frameworks work. It's simple, powerful, and includes all essential features like Dependency Injection, Middleware, Query Builder, and more!

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## � Features

- ✅ **RESTful Routing** - Annotation-based routing with `@Step`
- ✅ **Dependency Injection** - Constructor and field injection with `@Inject`
- ✅ **Middleware** - Request/Response interceptors with `@Before` / `@After`
- ✅ **Query Builder** - Fluent, SQL-injection-safe database operations
- ✅ **Exception Handling** - Custom exception handlers with `@ExceptionHandler`
- ✅ **Configuration Injection** - Inject config values with `@ConfigValue`
- ✅ **Scheduled Tasks** - Background jobs with `@Scheduled`
- ✅ **Validation** - Bean validation with Hibernate Validator
- ✅ **Database Support** - MySQL, PostgreSQL, SQLite
- ✅ **JSON Auto-Serialization** - Automatic request/response JSON handling

---

## � Quick Start

### 1. Create Your Project Structure

```
my-app/
├── src/
│   └── MyController.java
└── artha.json
```

### 2. Configure `artha.json`

```json
{
  "port": 8080,
  "database": {
    "driver": "sqlite",
    "name": "app.db"
  },
  "app": {
    "name": "My App",
    "version": "1.0.0"
  }
}
```

### 3. Create Your Controller

```java
import dev.artha.annotations.*;
import dev.artha.http.*;
import java.util.Map;

@Step(path = "/api")
public class MyController {
    
    @Step(path = "/hello", method = "GET")
    public Object hello() {
        return Map.of("message", "Hello, ARTHA!");
    }
}
```

### 4. Compile and Run

```bash
# Compile
javac -cp artha-runtime-0.1.0.jar -d build src/*.java

# Run
java -cp "artha-runtime-0.1.0.jar;build" dev.artha.core.Runtime
```

🎉 Your API is now running at `http://localhost:8080/api/hello`

---

## 📖 Core Concepts

### Routing with `@Step`

```java
@Step(path = "/users")
public class UserController {
    
    // GET /users
    @Step(path = "", method = "GET")
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    // GET /users/{id}
    @Step(path = "/{id}", method = "GET")
    public User getUser(Request req) {
        String id = req.param("id");
        return userService.findById(id);
    }
    
    // POST /users
    @Step(path = "", method = "POST")
    public User createUser(Request req) {
        User user = req.body(User.class);
        return userService.save(user);
    }
}
```

### Dependency Injection with `@Inject`

```java
public class UserService {
    // Service implementation
}

@Step(path = "/users")
public class UserController {
    
    @Inject
    private UserService userService;  // Auto-injected!
    
    @Step(path = "", method = "GET")
    public List<User> getAll() {
        return userService.findAll();
    }
}
```

**All dependencies are singletons** - one instance shared across the app.

### Middleware with `@Before` / `@After`

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
    
    @Step(path = "/data", method = "GET")
    public Object getData() {
        return Map.of("secret", "data");
    }
}
```

### Query Builder (SQL-Safe)

```java
@Inject
private Database db;

// SELECT with WHERE and ORDER BY
List<Map<String, Object>> users = db.table("users")
    .where("age", ">", 18)
    .orderBy("name", "ASC")
    .limit(10)
    .get();

// INSERT
int id = db.table("products")
    .insert(Map.of(
        "name", "Laptop",
        "price", 999.99,
        "stock", 50
    ));

// UPDATE
db.table("products")
    .where("id", id)
    .update(Map.of("price", 899.99));

// DELETE
db.table("products")
    .where("id", id)
    .delete();
```

**All queries use prepared statements** - 100% SQL injection safe!

### Exception Handling with `@ExceptionHandler`

```java
public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }
}

@Step(path = "/api")
public class MyController {
    
    @Step(path = "/users/{id}", method = "GET")
    public User getUser(Request req) throws Exception {
        User user = userService.findById(req.param("id"));
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }
    
    // Exception Handler
    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFound(Exception e, Request req, Response res) {
        res.status(404);
        return Map.of(
            "error", "Not Found",
            "message", e.getMessage(),
            "path", req.path()
        );
    }
}
```

### Configuration Injection with `@ConfigValue`

**artha.json:**
```json
{
  "app": {
    "name": "My App",
    "maxUsers": 1000
  },
  "email": {
    "apiKey": "secret-key",
    "from": "noreply@example.com"
  }
}
```

**Service:**
```java
public class EmailService {
    
    @ConfigValue("email.apiKey")
    private String apiKey;
    
    @ConfigValue("email.from")
    private String defaultSender;
    
    @ConfigValue("app.maxUsers")
    private int maxUsers;
    
    public void sendEmail(String to, String message) {
        // Use apiKey and defaultSender
    }
}
```

### Scheduled Tasks with `@Scheduled`

```java
public class BackgroundJobs {
    
    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        System.out.println("🧹 Cleaning up expired sessions...");
        // Cleanup logic
    }
    
    // Runs every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void sendDailyReport() {
        System.out.println("📧 Sending daily report...");
        // Report logic
    }
}
```

**Methods must be `void` with no parameters.**

### Validation

```java
import jakarta.validation.constraints.*;

public class User {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email")
    private String email;
    
    @Min(value = 18, message = "Must be 18+")
    private int age;
}

@Step(path = "/users", method = "POST")
public User createUser(@Valid User user) {
    return userService.save(user);
}
```

---

## 🗄️ Database Configuration

### SQLite (Simple, No Server)

```json
{
  "database": {
    "driver": "sqlite",
    "name": "app.db"
  }
}
```

### MySQL

```json
{
  "database": {
    "driver": "mysql",
    "host": "localhost",
    "port": 3306,
    "name": "mydb",
    "username": "root",
    "password": "password"
  }
}
```

### PostgreSQL

```json
{
  "database": {
    "driver": "postgresql",
    "host": "localhost",
    "port": 5432,
    "name": "mydb",
    "username": "postgres",
    "password": "password"
  }
}
```

---

## � Project Structure

```
artha/
├── runtime/                    # Framework core
│   ├── src/main/java/
│   │   └── dev/artha/
│   │       ├── annotations/    # @Step, @Inject, @Before, etc.
│   │       ├── core/           # Runtime, DIContainer, etc.
│   │       ├── db/             # Database, QueryBuilder
│   │       └── http/           # Request, Response, Middleware
│   └── pom.xml
├── examples/
│   └── comprehensive-demo/     # Full-featured demo app
└── README.md
```

---

## 🎯 Complete Example

See `examples/comprehensive-demo/` for a full application demonstrating:
- ✅ REST API with CRUD operations
- ✅ Middleware for logging
- ✅ Dependency Injection
- ✅ Query Builder for database
- ✅ Exception handlers
- ✅ Configuration injection
- ✅ Scheduled background tasks

---

## 🛠️ Building from Source

```bash
# Build runtime
cd runtime
mvn clean package

# Output: runtime/target/artha-runtime-0.1.0.jar
```

---

## 📚 API Reference

### Request Object

```java
// Query parameters
String name = req.query("name");
String page = req.query("page", "1");  // with default

// Path parameters
String id = req.param("id");

// Headers
String auth = req.header("Authorization");

// JSON body
User user = req.body(User.class);
Map<String, Object> data = req.bodyAsMap();

// Metadata
String method = req.method();  // GET, POST, etc.
String path = req.path();      // /api/users
String ip = req.ip();
```

### Response Object

```java
// Set status
res.status(201);
res.status(404);

// Set headers
res.header("X-Custom", "value");

// JSON response (automatic)
return Map.of("message", "Success");
return user;  // Auto-serialized to JSON
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

MIT License - feel free to use this for education!

---

## 🙏 Acknowledgments

Inspired by:
- **Spring Boot** - For the annotation-driven approach
- **Laravel** - For the elegant Query Builder
- **Express.js** - For middleware simplicity

Built with ❤️ for students learning backend development!

---

## 📞 Support

For questions or issues, please open an issue on GitHub or contact the maintainers.

**Happy Coding! 🚀**


```
+-------------------------------------------------------------+
|         ARTHA FRAMEWORK ARCHITECTURE                        |
+-------------------------------------------------------------+

  +---------------+  +---------------+  +----------+
  |   CLI Tool    |  |   Runtime     |  | User Code|
  |   (Node.js)   |->|    (Java)     |<-| (Java)   |
  +-------+-------+  +-------+-------+  +------+----+
          |                  |                  |
          +--------+---------+----------+-------+
                   |          |         |
          +--------+--+  +----+-----+  +---+-------+
          |  Scanner |  | Javalin  |  |@Step     |
          |  Manager |  | Runtime  |  |@Inject   |
          +----------+  | Injector |  | Methods  |
                        +----+-----+  +----------+
                             |
            +----------------+----------------+
            |                |                |
        +---+---+     +-------+-------+  +-----+-------+
        |   DI  |     |   Database    |  |  Middleware|
        |Container   |   Manager     |  |   System   |
        +-------+     | QueryBuilder  |  +-------------+
                      +-------+-------+
                              |
                        +-----+-----+
                        |    HTTP   |
                        |   Server  |
                        |   :8080   |
                        +-----------+
```
