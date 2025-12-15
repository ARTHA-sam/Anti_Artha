# 🎓 ARTHA Quick Reference

## All Annotations Cheat Sheet

```java
// 1. ROUTING - Define API endpoints
@Step(path = "/api")
public class MyController {
    @Step(path = "/hello", method = "GET")
    public String hello() { return "Hi!"; }
}

// 2. DEPENDENCY INJECTION - Auto-inject services
@Inject
private UserService userService;  // Automatically created & injected

// 3. CONFIG INJECTION - From artha.json
@ConfigValue("app.name")
private String appName;  // Reads from {"app": {"name": "..."}}

// 4. MIDDLEWARE - Run code before/after
@Before({AuthMiddleware.class})  // Before all endpoints
@After({LogMiddleware.class})    // After all endpoints

// 5. EXCEPTION HANDLING - Catch errors
@ExceptionHandler(NotFoundException.class)
public Object handleError(Exception e) {
    return Map.of("error", e.getMessage());
}

// 6. BACKGROUND TASKS - Run periodically
@Scheduled(fixedRate = 60000)  // Every 60 seconds
public void cleanup() { /* ... */ }

// 7. VALIDATION - Validate input
public User create(@Valid User user) { /* ... */ }
```

---

## Database Operations (QueryBuilder)

```java
// SELECT
List<User> users = db.table("users")
    .where("age", ">", 18)
    .orderBy("name", "ASC")
    .limit(10)
    .get(User.class);

// INSERT
int id = db.table("users")
    .insert(Map.of("name", "John", "age", 25));

// UPDATE
db.table("users")
    .where("id", id)
    .update(Map.of("age", 26));

// DELETE
db.table("users")
    .where("id", id)
    .delete();
```

---

## Project Structure

```
my-app/
├── src/
│   ├── MyController.java    # Your code
│   ├── MyService.java
│   └── MyModel.java
├── artha.json                # Config file
└── build/                    # Compiled files
```

---

## Next Steps: Build a Real Project!

Let's create a **Blog API** with:
- ✅ Users (CRUD)
- ✅ Posts (CRUD)
- ✅ Authentication
- ✅ Database
- ✅ Error handling
- ✅ Scheduled cleanup

**Ready?** 🚀
