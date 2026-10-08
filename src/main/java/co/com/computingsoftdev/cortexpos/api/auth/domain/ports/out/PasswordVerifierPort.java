package co.com.computingsoftdev.cortexpos.api.auth.domain.ports.out;

public interface PasswordVerifierPort {
    boolean matches(String rawPassword, String passwordHash);

    void simulateVerification(String rawPassword);
}
