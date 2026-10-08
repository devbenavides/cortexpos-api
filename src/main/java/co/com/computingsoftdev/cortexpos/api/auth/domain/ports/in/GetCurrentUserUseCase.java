package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.auth.domain.model.UserProfile;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    UserProfile getCurrentUser(UUID userUuid);
}
