import dev.artha.annotations.*;
import dev.artha.db.Database;
import java.util.List;
import java.util.Map;

/**
 * ARTHA v2.0 Demo - LeetCode-Style Simplicity!
 * 
 * This example shows how simple API development is with ARTHA v2.0:
 * - No Request/Response boilerplate
 * - Auto parameter extraction
 * - Auto type conversion
 * - REST conventions
 */

// Simple User model
class User {
    public int id;
    public String name;
    public String email;
    public int age;

    public User() {
    }

    public User(int id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }
}

// ARTHA v2.0: Convention-based REST Controller!
@RestController("/users")
public class UserController {
    @Inject
    private Database db;

    // GET /users - Auto-detected from method name!
    public List<User> list() throws Exception {
        return db.table("users").get(User.class);
    }

    // GET /users/{id} - Auto parameter extraction!
    public User getById(int id) throws Exception {
        return db.table("users").where("id", id).first(User.class);
    }

    // POST /users - Auto body parsing!
    @Status(201)
    public Map<String, Integer> create(@Body User user) throws Exception {
        int id = db.table("users").insert(Map.of(
                "name", user.name,
                "email", user.email,
                "age", user.age));
        return Map.of("id", id);
    }

    // PUT /users/{id} - Auto parameters!
    public User update(int id, @Body User updates) throws Exception {
        db.table("users").where("id", id).update(Map.of(
                "name", updates.name,
                "email", updates.email,
                "age", updates.age));
        return db.table("users").where("id", id).first(User.class);
    }

    // DELETE /users/{id}
    @Status(204)
    public void delete(int id) throws Exception {
        db.table("users").where("id", id).delete();
    }

    // GET /users/by-email?email=test@example.com
    // Custom method with @Query
    public User getByEmail(@Query String email) throws Exception {
        return db.table("users").where("email", email).first(User.class);
    }

    // GET /users/search?name=John&minAge=18
    // Multiple query parameters with defaults
    public List<User> search(
            @Query(defaultValue = "") String name,
            @Query(defaultValue = "0") int minAge) throws Exception {
        return db.table("users")
                .where("name", "LIKE", "%" + name + "%")
                .where("age", ">=", minAge)
                .get(User.class);
    }
}

/*
 * LOOK HOW SIMPLE THIS IS! 🎉
 * 
 * Before v2.0:
 * public User getById(Request req) {
 * int id = Integer.parseInt(req.param("id"));
 * return db.table("users").where("id", id).first(User.class);
 * }
 * 
 * After v2.0:
 * public User getById(int id) {
 * return db.table("users").where("id", id).first(User.class);
 * }
 * 
 * AS SIMPLE AS LEETCODE! 🚀
 */
