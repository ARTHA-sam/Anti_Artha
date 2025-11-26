package demo;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.Map;

@Step(path = "/users", method = "POST")
public class UserApi {
    public Object handle(Request req, Response res) {
        // Example showing POST request handling
        String name = req.query("name", "Guest");

        res.status(201);

        return Map.of(
                "success", true,
                "message", "User created: " + name,
                "id", 101);
    }
}
