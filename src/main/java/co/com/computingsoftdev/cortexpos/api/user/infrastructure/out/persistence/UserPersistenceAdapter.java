package co.com.computingsoftdev.cortexpos.api.user.infrastructure.out.persistence;

import co.com.computingsoftdev.cortexpos.api.user.domain.model.User;
import co.com.computingsoftdev.cortexpos.api.user.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserPersistenceMapper mapper;

    @Override
    public Optional<User> findByLogin(String login) {
        return repository.findByLogin(login).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public void updateLastLogin(UUID userUuid, Instant at) {
        repository.updateLastLogin(userUuid, at);
    }
}
