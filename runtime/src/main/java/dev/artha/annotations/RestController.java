package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a RESTful controller with convention-based routing.
 * Methods are automatically mapped to HTTP endpoints based on their names.
 * 
 * Conventions:
 * - getById(id) or get(id) → GET /resource/{id}
 * - list() or getAll() → GET /resource
 * - create(...) → POST /resource
 * - update(id, ...) → PUT /resource/{id}
 * - delete(id) → DELETE /resource/{id}
 * 
 * Example:
 * 
 * <pre>
 * &#64;RestController("/users")
 * public class UserController {
 *     public User getById(int id) { ... }
 *     public List&lt;User&gt; list() { ... }
 *     public int create(@Body User user) { ... }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RestController {
    /**
     * Base path for all endpoints in this controller.
     */
    String value();
}
