package co.com.computingsoftdev.cortexpos.api.auth.domain.model;

import java.util.Set;
import java.util.UUID;

public record AuthUser(
        UUID uuid,
        String username,
        String email,
        String fullName,
        String passwordHash,
        boolean active,
        Set<String> roles,
        Set<String> permissions
) {
}
