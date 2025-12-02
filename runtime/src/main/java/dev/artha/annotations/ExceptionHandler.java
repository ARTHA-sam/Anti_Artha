package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an exception handler.
 * The method will be invoked when the specified exception(s) are thrown.
 * 
 * Handler method signature: (Exception e, Request req, Response res) -> Object
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionHandler {
    /**
     * The exception types this handler will catch
     */
    Class<? extends Exception>[] value();
}
