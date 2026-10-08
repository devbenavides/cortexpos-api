package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.auth.domain.model.AuthSession;
import co.com.computingsoftdev.cortexpos.api.auth.domain.ports.in.command.LoginCommand;

public interface LoginUseCase {
    AuthSession login(LoginCommand command);
}
