package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String code, String message) {
        super(code, message);
    }
}
