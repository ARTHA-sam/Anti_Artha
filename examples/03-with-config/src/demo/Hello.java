package demo;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;

@Step(path = "/hello")
public class Hello {
    public String handle(Request req, Response res) {
        return "Namaste from ARTHA!";
    }
}
