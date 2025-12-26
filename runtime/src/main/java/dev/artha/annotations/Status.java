package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets a custom HTTP status code for the response.
 * By default, successful responses return 200 OK.
 * 
 * Example:
 * 
 * <pre>
 * @Step(path = "/users", method = "POST")
 * &#64;Status(201) // Created
 * public User createUser(@Body User user) {
 *     return userService.save(user);
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Status {
    /**
     * HTTP status code to return.
     */
    int value();
}
