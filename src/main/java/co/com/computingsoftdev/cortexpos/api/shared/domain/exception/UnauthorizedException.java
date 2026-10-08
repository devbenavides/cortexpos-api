package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
