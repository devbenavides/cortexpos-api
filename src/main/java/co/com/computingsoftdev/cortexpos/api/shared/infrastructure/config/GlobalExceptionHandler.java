package co.com.computingsoftdev.cortexpos.api.shared.infrastructure.config;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.ApiError;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.DuplicateDocumentNumberException;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (msg1, msg2) -> msg1
                ));
        return buildValidationError(HttpStatus.BAD_REQUEST, "Existen errores en los campos enviados.", fieldErrors);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex){
        return buildBusinessError(HttpStatus.NOT_FOUND,ex.getMessage(), "notFound");
    }

    @ExceptionHandler(DuplicateDocumentNumberException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateDocumentNumber(DuplicateDocumentNumberException ex) {
        return buildBusinessError(HttpStatus.CONFLICT, ex.getMessage(), "conflict");
    }


    private ApiError buildValidationError(HttpStatus status, String message, Map<String, String> fieldErrors) {
        ApiError error = new ApiError();
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setType("validation");
        error.setMessage(message);
        error.setFieldErrors(fieldErrors);
        return error;
    }

    private ApiError buildBusinessError(HttpStatus status, String message, String type) {
        ApiError error = new ApiError();
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setType(type);
        error.setMessage(message);
        return error;
    }
}
