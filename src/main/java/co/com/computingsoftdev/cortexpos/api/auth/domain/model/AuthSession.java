package co.com.computingsoftdev.cortexpos.api.auth.domain.model;

public record AuthSession(AuthTokens tokens, UserProfile user) {
}
