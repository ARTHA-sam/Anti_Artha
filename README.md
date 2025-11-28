# 🚀 ARTHA - Java Backend Made Simple

> A zero-boilerplate, ultra-lightweight Java backend framework that brings Flask-like simplicity to the Java ecosystem.

**Status**: ✨ Early Stage | **License**: MIT

---

## 📖 What is ARTHA?

ARTHA is designed to remove the complexity that makes Java overwhelming for newcomers. It abstracts away build tools, dependency injection, and server configuration, letting you focus purely on logic.

**Why ARTHA?**
- ⚡ **< 3s Startup**: Faster than Spring Boot.
- 📦 **Zero Config**: No `pom.xml`, `build.gradle`, or XML files.
- 🎯 **Hot Reload**: Changes apply instantly.
- 🛠️ **Smart Injection**: Automatically injects what you need (`Request`, `Response`, `Connection`, etc.).

---

## 🚀 Quick Start

### 1. Installation
Install the CLI tool globally:
```bash
npm install -g artha-cli
# Or from GitHub
npm install -g github:ARTHA-sam/Artha_CLI
```

### 2. Create a Project
```bash
artha new my-app
cd my-app
```

### 3. Run Dev Server
```bash
artha dev
```
Open `http://localhost:8080/hello` to see your app!

---

## ✨ Key Features

### 1. The `@Step` Annotation
Define routes with a single annotation. No separate controller classes or router configuration needed.

```java
@Step(path = "/hello", method = "GET")
public class Hello {
    public String handle() {
        return "Hello World!";
    }
}
```

### 2. 🧠 Smart Parameter Injection
ARTHA automatically detects what your `handle` method needs and injects it. You don't need to conform to a strict signature.

**Supported Parameters:**
- `Request req`: Access query params, headers, body.
- `Response res`: Set status codes, headers.
- `Connection db`: **(New!)** Auto-injected JDBC connection (if DB is configured).
- `MyDto body`: **(New!)** Auto-parsed JSON body if you define a POJO parameter.

### 3. 💾 Zero-Config Database
Built-in support for SQLite. Just add it to `artha.json` and start using it.

**artha.json:**
```json
{
  "database": {
    "driver": "sqlite",
    "name": "mydb.db"
  }
}
```

**Usage:**
```java
@Step(path = "/users")
public class GetUsers {
    // DB Connection is automatically opened, injected, and closed!
    public Object handle(Connection db) throws SQLException {
        // Use standard JDBC
        return "DB Connected!";
    }
}
```

---

## 📚 Usage Examples

### 1. JSON API with Input Validation
ARTHA makes it easy to validate input and return JSON.

```java
@Step(path = "/users", method = "POST")
public class CreateUser {
    public Object handle(Request req, Response res) {
        User user = req.body(User.class);
        
        // Simple Validation
        if (user.getName() == null || user.getName().isEmpty()) {
            res.status(400);
            return Map.of("error", "Name is required");
        }

        return Map.of("status", "created", "user", user);
    }
}
```

### 2. Database CRUD (SQLite)
Full example of a database-backed endpoint.

```java
@Step(path = "/todos", method = "GET")
public class GetTodos {
    public List<Todo> handle(Connection db) throws SQLException {
        List<Todo> todos = new ArrayList<>();
        try (Statement stmt = db.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM todos");
            while (rs.next()) {
                todos.add(new Todo(rs.getString("title")));
            }
        }
        return todos; // Automatically serialized to JSON
    }
}
```

---

## ⚙️ Configuration (`artha.json`)

Manage your project settings in `artha.json`.

```json
{
  "name": "my-app",
  "version": "1.0.0",
  "server": {
    "port": 9090
  },
  "database": {
    "driver": "sqlite",
    "name": "data.db"
  },
  "dependencies": {
    "gson": "2.10.1",
    "slf4j-simple": "2.0.11"
  }
}
```

---

## 🛣️ Roadmap & Future Plans

We are constantly improving ARTHA. Here is what's done and what's coming:

- [x] **CLI Tool**: Scaffolding, Hot-Reload, Dependency Management.
- [x] **Smart Routing**: `@Step` annotation with method detection.
- [x] **Database Support**: Zero-config SQLite integration.
- [x] **Smart Injection**: Auto-inject `Connection`, `Request`, `Body`.
- [x] **User Validation**: Basic structural validation and helpers.
- [ ] **Authentication**: Built-in JWT support.
- [ ] **OpenAPI / Swagger**: Auto-generated API documentation.
- [ ] **Docker Support**: One-command containerization.
- [ ] **Testing Framework**: Simple integration testing tools.

---

## 🤝 Contributing

1. Fork the repo.
2. Create a branch (`feature/cool-thing`).
3. Commit changes.
4. Push and create a Pull Request.

Let's make Java fun again! 🚀
