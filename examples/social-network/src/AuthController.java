import dev.artha.annotations.*;
import dev.artha.http.*;
import dev.artha.db.Database;
import java.util.*;

@Step(path = "/api/auth")
public class AuthController {

    @Inject
    private Database db;

    @Step(path = "/register", method = "POST")
    public Object register(Request req, Response res) {
        try {
            // New ORM Way!
            User user = req.body(User.class);

            // Check if user exists (Manual query still useful for specific logic)
            // But we can verify ORM power:
            List<Map<String, Object>> existing = db.execute(
                    "SELECT id FROM users WHERE username = ? OR email = ?",
                    user.getUsername(), user.getEmail());

            if (!existing.isEmpty()) {
                res.status(400);
                return Map.of("error", "Username or email already exists");
            }

            // Save user using ORM
            int userId = db.table("users").save(user);

            String token = "user_" + userId + "_token";

            res.status(201);
            return Map.of("token", token, "userId", userId, "username", user.getUsername());

        } catch (Exception e) {
            res.status(500);
            return Map.of("error", "Register failed: " + e.getMessage());
        }
    }

    // New Secure Endpoint using Middleware
    @Step(path = "/me", method = "GET")
    @Before(AuthMiddleware.class)
    public Object me(Request req) {
        // Code here runs ONLY if AuthMiddleware passes!
        // No manual if checks needed.
        return Map.of("message", "You are authenticated!", "user", "UserORM");
    }

    @Step(path = "/login", method = "POST")
    public Object login(Request req, Response res) {
        try {
            Map<String, Object> body = req.bodyAsMap();
            String username = (String) body.get("username");
            String password = (String) body.get("password");

            List<Map<String, Object>> users = db.execute(
                    "SELECT id, username, full_name FROM users WHERE username = ? AND password = ?",
                    username, password);

            if (users.isEmpty()) {
                res.status(401);
                return Map.of("error", "Invalid credentials");
            }

            Map<String, Object> user = users.get(0);
            int userId = ((Number) user.get("id")).intValue();
            String token = "user_" + userId + "_token";

            return Map.of("token", token, "userId", userId, "username", user.get("username"));

        } catch (Exception e) {
            res.status(500);
            return Map.of("error", e.getMessage());
        }
    }
}
