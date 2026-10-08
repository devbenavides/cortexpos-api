package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out;

import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthUser;

import java.util.Optional;
import java.util.UUID;

public interface UserLookupPort {
    Optional<AuthUser> findByLogin(String login);

    Optional<AuthUser> findByUuid(UUID uuid);

    void registerLogin(UUID userUuid);
}
