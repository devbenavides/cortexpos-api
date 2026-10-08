package co.com.computingsoftdev.cortexpos.api.user.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface FindUserUseCase {
    Optional<User> findByLogin(String login);
    Optional<User> findByUuid(UUID uuid);
}
