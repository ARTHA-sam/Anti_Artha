# 🚀 ARTHA v2.0 - LeetCode-Style Backend Development!

**The Simplest Way to Build APIs in Java**

---

## ✨ What's New in v2.0

### **1. Auto Parameter Extraction**
No more manual parsing! Parameters are automatically extracted and type-converted.

```java
// Before v1.0
public User getUser(Request req) {
    int id = Integer.parseInt(req.param("id"));  // Manual!
    return db.table("users").where("id", id).first(User.class);
}

// After v2.0  
public User getById(int id) {  // Auto-extracted! ✨
    return db.table("users").where("id", id).first(User.class);
}
```

### **2. @RestController Convention**
Methods are automatically mapped to HTTP endpoints based on their names!

```java
@RestController("/users")
public class UserController {
    // Conventions:
    getById(int id)  → GET /users/{id}
    list()           → GET /users
    create(User u)   → POST /users
    update(int id)   → PUT /users/{id}
    delete(int id)   → DELETE /users/{id}
}
```

### **3. @Body Annotation**
Automatic JSON parsing to objects!

```java
public int create(@Body User user) {  // Auto-parsed from JSON!
    return db.table("users").insert(user);
}
```

### **4. @Query Annotation**
Clean query parameter handling with defaults!

```java
public List<User> search(
    @Query(defaultValue = "") String name,
    @Query(defaultValue = "0") int minAge
) {
    return db.table("users")
        .where("age", ">=", minAge)
        .get(User.class);
}
```

### **5. @Status Annotation**
Custom status codes made easy!

```java
@Status(201)  // Created
public User create(@Body User user) {
    return userService.save(user);
}
```

---

## 🎯 Quick Start

### 1. Setup Database
```bash
sqlite3 users.db < schema.sql
```

### 2. Run the Server
```bash
cd examples/v2-demo
artha dev
```

### 3. Test the API

**Get all users:**
```bash
curl http://localhost:8080/users
```

**Get user by ID:**
```bash
curl http://localhost:8080/users/1
```

**Create user:**
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","age":28}'
```

**Update user:**
```bash
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"John Updated","email":"john@example.com","age":26}'
```

**Delete user:**
```bash
curl -X DELETE http://localhost:8080/users/1
```

**Search by email:**
```bash
curl "http://localhost:8080/users/by-email?email=jane@example.com"
```

**Search with filters:**
```bash
curl "http://localhost:8080/users/search?name=John&minAge=20"
```

---

## 📊 Code Comparison

### Before v1.0 (verbose):
```java
@Step(path = "/users", method = "POST")
public Object createUser(Request req, Response res) throws Exception {
    Map<String, Object> body = req.bodyAsMap();
    User user = new User();
    user.setName((String) body.get("name"));
    user.setEmail((String) body.get("email"));
    user.setAge((Integer) body.get("age"));
    
    int id = db.table("users").insert(Map.of(
        "name", user.getName(),
        "email", user.getEmail(),
        "age", user.getAge()
    ));
    
    res.status(201);
    return Map.of("id", id);
}
```
**Lines:** 16

### After v2.0 (clean):
```java
@Status(201)
public Map<String, Integer> create(@Body User user) throws Exception {
    int id = db.table("users").insert(Map.of(
        "name", user.name,
        "email", user.email,
        "age", user.age
    ));
    return Map.of("id", id);
}
```
**Lines:** 8

**50% less code!** 🎉

---

## 🎓 Convention Mapping

| Method Name | HTTP Method | Path Pattern | Example |
|-------------|-------------|--------------|---------|
| `getById(int id)` | GET | `/resource/{id}` | GET /users/123 |
| `get(long id)` | GET | `/resource/{id}` | GET /users/456 |
| `list()` | GET | `/resource` | GET /users |
| `getAll()` | GET | `/resource` | GET /users |
| `findAll()` | GET | `/resource` | GET /users |
| `create(...)` | POST | `/resource` | POST /users |
| `add(...)` | POST | `/resource` | POST /users |
| `update(int id, ...)` | PUT | `/resource/{id}` | PUT /users/123 |
| `delete(int id)` | DELETE | `/resource/{id}` | DELETE /users/123 |
| `remove(int id)` | DELETE | `/resource/{id}` | DELETE /users/123 |
| `getByEmail(...)` | GET | `/resource/by-email` | GET /users/by-email |
| `findActive()` | GET | `/resource/active` | GET /users/active |

---

## ✨ Features Showcase

### Auto Type Conversion
```java
public User getById(int id) { }          // String → int
public User getByEmail(String email) { } // String → String
public void activate(boolean active) { }  // String → boolean
```

Supported types:
- ✅ int, Integer
- ✅ long, Long
- ✅ double, Double
- ✅ float, Float
- ✅ boolean, Boolean
- ✅ String

### Query Parameters with Defaults
```java
public List<User> paginate(
    @Query(defaultValue = "1") int page,
    @Query(defaultValue = "10") int limit
) {
    return db.table("users")
        .limit(limit)
        .offset((page - 1) * limit)
        .get(User.class);
}
```

### Complex Searches
```java
public List<User> advancedSearch(
    @Query String name,
    @Query(defaultValue = "0") int minAge,
    @Query(defaultValue = "100") int maxAge,
    @Query(defaultValue = "false") boolean active
) {
    return db.table("users")
        .where("name", "LIKE", "%" + name + "%")
        .where("age", ">=", minAge)
        .where("age", "<=", maxAge)
        .where("active", active)
        .get(User.class);
}
```

---

## 🎯 Backward Compatibility

v1.0 code still works!

```java
// Old @Step style - STILL WORKS ✅
@Step(path = "/old-way/{id}", method = "GET")
public User oldWay(Request req, Response res) {
    String id = req.param("id");
    return userService.findById(id);
}

// New v2.0 style - RECOMMENDED ✅
public User getById(int id) {
    return userService.findById(id);
}
```

---

## 🚀 Benefits

1. **50% less code** - No boilerplate
2. **Type-safe** - Automatic type conversion
3. **Intuitive** - Method names → endpoints
4. **Clean** - Focus on logic, not framework
5. **LeetCode-style** - As simple as solving DSA problems!

---

**Built with ❤️ for students learning backend development**

**ARTHA v2.0 - Where Backend Meets LeetCode Simplicity!** 🎉
