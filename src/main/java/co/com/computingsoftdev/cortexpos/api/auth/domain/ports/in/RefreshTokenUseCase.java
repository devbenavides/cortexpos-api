package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthSession;

public interface RefreshTokenUseCase {
    AuthSession refresh(String refreshToken);
}
