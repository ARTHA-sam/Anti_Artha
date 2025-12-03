package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schedule a method to run at fixed intervals.
 * Method must be void with no parameters.
 * 
 * Example: @Scheduled(fixedRate = 5000) // Every 5 seconds
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Scheduled {
    /**
     * Fixed rate in milliseconds between executions
     */
    long fixedRate();
}
