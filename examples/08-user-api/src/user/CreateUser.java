package user;

import dev.artha.annotations.Step;

@Step(path = "/users", method = "POST")
public class CreateUser {
    public User handle(User user) {
        // Echo the user back with a success message
        System.out.println("✅ Received user: " + user.getName() + " (" + user.getEmail() + ")");
        return user;
    }
}
