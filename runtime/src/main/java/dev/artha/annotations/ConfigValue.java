package dev.artha.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject configuration values from artha.json into fields.
 * Use dot notation to access nested properties.
 * 
 * Example: @ConfigValue("email.apiKey")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    /**
     * The configuration key path (dot notation)
     */
    String value();
}
