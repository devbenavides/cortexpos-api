package co.com.computingsoftdev.cortexpos.api.user.domain.ports.in;

import java.util.UUID;

public interface RegisterLoginUseCase {
    void registerLogin(UUID uuid);
}
