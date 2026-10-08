package co.com.computingsoftdev.cortexpos.api.auth.domain.model;

public record AuthTokens(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
