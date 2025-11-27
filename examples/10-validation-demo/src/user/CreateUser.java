package user;

import dev.artha.annotations.Step;
import dev.artha.annotations.Valid;

@Step(path = "/users", method = "POST")
public class CreateUser {

    // The @Valid annotation triggers automatic validation
    // If validation fails, framework returns 400 Bad Request automatically
    public User handle(@Valid User user) {
        // If we reach here, user is guaranteed to be valid!
        System.out.println("✅ Valid user created: " + user.getName());
        return user;
    }
}
