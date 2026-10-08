package co.com.computingsoftdev.cortexpos.api.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class User {
    private final Long id;
    private final UUID uuid;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final String firstName;
    private final String lastName;
    private final Instant lastLoginAt;

    @Builder.Default
    private final Set<Role> roles = Set.of();

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    /** Solo los roles activos cuentan. */
    public Set<String> roleNames() {
        return roles.stream()
                .filter(Role::isActive)
                .map(Role::getName)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Unión de los permisos de todos los roles activos. */
    public Set<String> permissionNames() {
        return roles.stream()
                .filter(Role::isActive)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}
