package co.com.computingsoftdev.cortexpos.api.shared.infrastructure.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ObjectsValidator {
    private final Validator validator;

    // El parámetro genérico <T> va AQUÍ en el método:
    public <T> void validate(T objectToValidate) {
        Set<ConstraintViolation<T>> violations = validator.validate(objectToValidate);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
