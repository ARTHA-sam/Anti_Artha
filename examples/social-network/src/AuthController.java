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
            Map<String, Object> body = req.bodyAsMap();
            String username = (String) body.get("username");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String fullName = (String) body.get("fullName");

            // Check if user exists
            List<Map<String, Object>> existing = db.execute(
                    "SELECT id FROM users WHERE username = ? OR email = ?",
                    username, email);

            if (!existing.isEmpty()) {
                res.status(400);
                return Map.of("error", "Username or email already exists");
            }

            // Insert user
            int userId = db.table("users").insert(Map.of(
                    "username", username,
                    "email", email,
                    "password", password,
                    "full_name", fullName));

            String token = "user_" + userId + "_token";

            res.status(201);
            return Map.of("token", token, "userId", userId, "username", username);

        } catch (Exception e) {
            res.status(500);
            return Map.of("error", e.getMessage());
        }
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
