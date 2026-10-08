package co.com.computingsoftdev.cortexpos.api.auth.application;

import co.com.computingsoftdev.cortexpos.api.auth.domain.exception.AccountDisabledException;
import co.com.computingsoftdev.cortexpos.api.auth.domain.exception.InvalidCredentialsException;
import co.com.computingsoftdev.cortexpos.api.auth.domain.exception.InvalidTokenException;
import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthSession;
import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthTokens;
import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthUser;
import co.com.computingsoftdev.cortexpos.api.auth.domain.model.UserProfile;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in.GetCurrentUserUseCase;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in.LoginUseCase;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in.RefreshTokenUseCase;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in.command.LoginCommand;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out.PasswordVerifierPort;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out.TokenProviderPort;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService  implements LoginUseCase, RefreshTokenUseCase, GetCurrentUserUseCase {

    private final UserLookupPort users;
    private final PasswordVerifierPort passwordVerifier;
    private final TokenProviderPort tokenProvider;

    @Override
    public AuthSession login(LoginCommand command) {
        Optional<AuthUser> found = users.findByLogin(command.username());

        if (found.isEmpty()) {
            passwordVerifier.simulateVerification(command.password());
            throw new InvalidCredentialsException();
        }

        AuthUser user = found.get();
        if (!passwordVerifier.matches(command.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        // Solo después de acertar la contraseña se informa que la cuenta está desactivada
        if (!user.active()) {
            throw new AccountDisabledException();
        }

        users.registerLogin(user.uuid());
        return buildSession(user);
    }

    @Override
    public AuthSession refresh(String refreshToken) {
        UUID userUuid = tokenProvider.verifyRefreshToken(refreshToken);

        AuthUser user = users.findByUuid(userUuid)
                .orElseThrow(() -> new InvalidTokenException("El usuario del token ya no existe"));
        if (!user.active()) {
            throw new AccountDisabledException();
        }
        // Se recargan roles y permisos desde la BD: el nuevo access token refleja cambios recientes
        return buildSession(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getCurrentUser(UUID userUuid) {
        return users.findByUuid(userUuid)
                .map(UserProfile::from)
                .orElseThrow(()->new InvalidTokenException("The token user no longer exists."));
    }

    private AuthSession buildSession(AuthUser user) {
        AuthTokens tokens = tokenProvider.issueTokens(user);
        return new AuthSession(tokens, UserProfile.from(user));
    }
}
