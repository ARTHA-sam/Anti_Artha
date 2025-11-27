package dev.artha.core;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    private final Set<ConstraintViolation<Object>> violations;

    public ValidationException(Set<ConstraintViolation<Object>> violations) {
        super("Validation failed");
        this.violations = violations;
    }

    public Set<ConstraintViolation<Object>> getViolations() {
        return violations;
    }
}
