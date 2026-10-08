package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super("RESOURCE_CONFLICT", message);
    }

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
