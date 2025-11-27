package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter for automatic validation using Jakarta Bean Validation.
 * 
 * Example:
 * 
 * <pre>
 * {@code @Step(path = "/users", method = "POST")
 * public User createUser( @Valid User user) {
 *     // user is guaranteed to be valid here
 *     return user;
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Valid {
}
