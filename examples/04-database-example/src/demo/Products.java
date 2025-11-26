package demo;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;
import java.util.List;
import java.util.Map;

@Step(path = "/products", method = "GET")
public class Products {
    public Object handle(Request req, Response res) {
        // Example: would connect to PostgreSQL here
        // using dependency loaded via artha.json

        return Map.of(
                "products", List.of(
                        Map.of("id", 1, "name", "Laptop", "price", 45000),
                        Map.of("id", 2, "name", "Mouse", "price", 500),
                        Map.of("id", 3, "name", "Keyboard", "price", 1200)),
                "total", 3);
    }
}
