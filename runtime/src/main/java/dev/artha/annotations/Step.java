package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ARTHA - Marks a class as an API endpoint
 *
 * Example:
 * 
 * @Step(path = "/hello", method = "GET")
 *            public class Hello {
 *            public String handle(Request req, Response res) {
 *            return "Namaste!";
 *            }
 *            }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Step {
    String path();

    String method() default "GET";
}
