package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class ResourceNotFoundException extends DomainException{
    public ResourceNotFoundException(String resource, Object identifier) {
        super("RESOURCE_NOT_FOUND", "%s no encontrado: %s".formatted(resource, identifier));
    }
}
