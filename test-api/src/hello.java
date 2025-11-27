import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;

@Step(path = "/hello", method = "GET")
public class hello {
    String a() {
        return "Hello";
    }
}