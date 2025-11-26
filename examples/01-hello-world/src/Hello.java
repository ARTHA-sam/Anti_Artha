import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;

@Step(path = "/namaste", method = "GET")
public class Hello {
    public String handle(Request req, Response res) {
        String name = req.query("name", "Dost");
        return "sharayu, " + name + "! 🙏";
    }
}
