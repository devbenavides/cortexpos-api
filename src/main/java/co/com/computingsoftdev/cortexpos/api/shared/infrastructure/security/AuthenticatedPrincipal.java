package co.com.computingsoftdev.cortexpos.api.shared.infrastructure.security;

import java.util.UUID;

public record AuthenticatedPrincipal(UUID uuid, String username) {
}
