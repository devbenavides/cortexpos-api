package co.com.computingsoftdev.cortexpos.api.auth.domain.exception;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Incorrect username or password.");
    }
}
