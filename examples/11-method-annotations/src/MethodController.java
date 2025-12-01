package dev.artha.examples;

import dev.artha.annotations.Step;
import dev.artha.http.Request;
import dev.artha.http.Response;

@Step(path = "/api/v1")
public class MethodController {

    @Step(path = "/hello")
    public String hello() {
        return "Hello from nested path!";
    }

    @Step(path = "/users/{id}")
    public String getUser(Request req) {
        return "User ID: " + req.param("id");
    }

    @Step(path = "/root", method = "POST")
    public String postRoot() {
        return "Posted to root";
    }
}
