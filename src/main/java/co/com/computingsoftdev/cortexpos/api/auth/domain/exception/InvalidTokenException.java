package co.com.computingsoftdev.cortexpos.api.auth.domain.exception;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException() {
        this("Invalid or expired token");
    }

    public InvalidTokenException(String message) {
        super("INVALID_TOKEN ", message);
    }
}
