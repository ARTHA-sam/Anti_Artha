import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;

@Step(path = "/hello", method = "GET")
public class Hello {
    public String handle(Request req, Response res) {
        String name = req.query("name", "World");
        return "Hello, " + name + "! Welcome to ARTHA 🚀";
    }
}
