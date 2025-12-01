package dev.artha.annotations;

import dev.artha.http.Middleware;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies middleware to run BEFORE the main handler.
 * Can be applied to Classes or Methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Before {
    Class<? extends Middleware>[] value();
}
