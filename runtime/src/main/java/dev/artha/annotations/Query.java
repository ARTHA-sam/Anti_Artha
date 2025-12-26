package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameter to be automatically extracted from query parameters.
 * 
 * Example:
 * 
 * <pre>
 * &#64;Step(path = "/users", method = "GET")
 * public List&lt;User&gt; search(
 *         &#64;Query String name,
 *         &#64;Query(defaultValue = "1") int page,
 *         @Query(defaultValue = "10") int limit) {
 *     // name, page, limit auto-extracted from query params
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Query {
    /**
     * Custom query parameter name. If empty, uses the parameter name.
     */
    String value() default "";

    /**
     * Default value if query parameter is not provided.
     */
    String defaultValue() default "";
}
