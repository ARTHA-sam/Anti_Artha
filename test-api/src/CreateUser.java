import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.Map;

@Step(path = "/api/users", method = "POST")
public class CreateUser {

    public Object handle(Request req, Response res) {
        // Parse JSON body into User object
        // Example request body:
        // {
        // "name": "John Doe",
        // "email": "john@example.com",
        // "age": 25
        // }

        try {
            User user = req.body(User.class);

            // In real app, save to database using PostgreSQL dependency
            // Connection conn = DriverManager.getConnection(...)

            res.status(201);
            return Map.of(
                    "success", true,
                    "message", "User created successfully",
                    "user", user);
        } catch (Exception e) {
            res.status(400);
            return Map.of(
                    "success", false,
                    "error", "Invalid request body: " + e.getMessage());
        }
    }

    // Simple POJO for demonstration
    public static class User {
        private String name;
        private String email;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
