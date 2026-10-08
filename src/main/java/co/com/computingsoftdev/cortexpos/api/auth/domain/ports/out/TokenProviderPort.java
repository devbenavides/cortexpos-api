package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out;

import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthTokens;
import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthUser;

import java.util.UUID;

public interface TokenProviderPort {
    AuthTokens issueTokens(AuthUser user);
    UUID verifyRefreshToken(String refreshToken);
}
