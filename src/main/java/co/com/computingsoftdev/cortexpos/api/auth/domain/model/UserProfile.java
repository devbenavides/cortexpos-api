package co.com.computingsoftdev.cortexpos.api.auth.domain.model;

import java.util.Set;
import java.util.UUID;

public record UserProfile(
        UUID uuid,
        String username,
        String email,
        String fullName,
        Set<String> roles,
        Set<String> permissions
) {
    public static UserProfile from(AuthUser user) {
        return new UserProfile(user.uuid(), user.username(), user.email(), user.fullName(),
                user.roles(), user.permissions());
    }
}
