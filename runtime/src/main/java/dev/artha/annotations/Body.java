package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter to be injected from the request body.
 * The framework will automatically parse JSON from the request body
 * and map it to the annotated parameter type.
 * 
 * Example:
 * 
 * <pre>
 * @Step(path = "/users", method = "POST")
 * public User createUser(@Body User user) {
 *     return userService.save(user);
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Body {
}
