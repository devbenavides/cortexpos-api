package co.com.computingsoftdev.cortexpos.api.user.application;

import co.com.computingsoftdev.cortexpos.api.user.domain.model.User;
import co.com.computingsoftdev.cortexpos.api.user.domain.ports.in.FindUserUseCase;
import co.com.computingsoftdev.cortexpos.api.user.domain.ports.in.RegisterLoginUseCase;
import co.com.computingsoftdev.cortexpos.api.user.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSevice implements FindUserUseCase, RegisterLoginUseCase {

    private final UserRepositoryPort repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        return repository.findByLogin(login.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid);
    }

    @Override
    public void registerLogin(UUID uuid) {
        repository.updateLastLogin(uuid, Instant.now());
    }
}
