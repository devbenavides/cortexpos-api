package co.com.computingsoftdev.cortexpos.api.person.domain.ports.in;

import java.util.UUID;

public interface DeletePersonUseCase {
    void delete (UUID uuid);
}
