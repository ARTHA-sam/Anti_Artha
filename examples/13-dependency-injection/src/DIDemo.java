package dev.artha.examples;

import dev.artha.annotations.Inject;
import dev.artha.annotations.Step;

@Step(path = "/users")
public class DIDemo {

    @Inject
    private UserService userService;

    @Step(path = "")
    public Object getAllUsers() {
        return userService.getAllUsers();
    }

    @Step(path = "/{id}")
    public Object getUserById(dev.artha.http.Request req) {
        String id = req.param("id");
        return userService.getUserById(id);
    }
}
