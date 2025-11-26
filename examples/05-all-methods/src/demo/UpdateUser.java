package demo;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.Map;

@Step(path = "/users/:id", method = "PATCH")
public class UpdateUser {
    public Object handle(Request req, Response res) {
        // Example showing PATCH method support
        String userId = req.path("id");

        return Map.of(
                "success", true,
                "message", "User " + userId + " updated",
                "method", "PATCH");
    }
}
