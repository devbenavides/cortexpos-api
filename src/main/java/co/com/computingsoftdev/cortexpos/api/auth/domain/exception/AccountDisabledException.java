package co.com.computingsoftdev.cortexpos.api.auth.domain.exception;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.ForbiddenException;

public class AccountDisabledException extends ForbiddenException {
    public AccountDisabledException() {
        super("ACCOUNT_DISABLED","The account is deactivated");
    }
}
