package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
