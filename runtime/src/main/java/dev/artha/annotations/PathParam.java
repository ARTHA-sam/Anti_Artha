package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter to be extracted from path parameters.
 * This is optional - parameters are auto-extracted by name.
 * 
 * Example:
 * 
 * <pre>
 * @Step(path = "/users/{userId}", method = "GET")
 * public User getUser(@PathParam("userId") int id) {
 *     // id extracted from {userId} path param
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathParam {
    /**
     * Path parameter name. If empty, uses the parameter name.
     */
    String value() default "";
}
