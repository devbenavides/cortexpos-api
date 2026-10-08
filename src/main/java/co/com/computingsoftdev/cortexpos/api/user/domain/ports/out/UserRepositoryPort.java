package co.com.computingsoftdev.cortexpos.api.user.domain.ports.out;

import co.com.computingsoftdev.cortexpos.api.user.domain.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByLogin(String login);

    Optional<User> findByUuid(UUID uuid);

    void updateLastLogin(UUID userUuid, Instant at);
}
