package co.com.computingsoftdev.cortexpos.api.shared.infrastructure.config;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.ApiError;
import co.com.computingsoftdev.cortexpos.api.person.domain.exception.DuplicateDocumentNumberException;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Validaciones de DTOs en Controllers (@Valid @RequestBody)
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

    // 2. Validaciones en parámetros de la URL / Query params (@Validated @PathVariable / @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (msg1, msg2) -> msg1
                ));

        return buildValidationError(HttpStatus.BAD_REQUEST, "Existen errores en los campos enviados.", fieldErrors);
    }

    // 3. JSON mal formado o tipos de datos inválidos en el Body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildBusinessError(HttpStatus.BAD_REQUEST, "El cuerpo de la petición (JSON) está mal formado o contiene tipos de datos inválidos.", "malformed_json");
    }

    // 4. Recursos no encontrados (404)
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex){
        return buildBusinessError(HttpStatus.NOT_FOUND,ex.getMessage(), "notFound");
    }

    // 5. Excepciones de negocio personalizadas (409)
    @ExceptionHandler(DuplicateDocumentNumberException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateDocumentNumber(DuplicateDocumentNumberException ex) {
        return buildBusinessError(HttpStatus.CONFLICT, ex.getMessage(), "conflict");
    }

    // 6. Violación de restricciones de Base de Datos (409) - Ej. llaves duplicadas a nivel BD
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return buildBusinessError(HttpStatus.CONFLICT, "El registro ya existe o viola una restricción de integridad en la base de datos.", "data_integrity_violation");
    }

    // 7. Red de seguridad final para cualquier error no controlado (500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception ex) {
        return buildBusinessError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno e inesperado en el servidor.", "internal_error");
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
