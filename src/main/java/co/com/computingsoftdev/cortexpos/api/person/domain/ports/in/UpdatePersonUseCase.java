package co.com.computingsoftdev.cortexpos.api.person.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;

import java.util.UUID;

public interface UpdatePersonUseCase {
    Person update(UUID uuid, Person personToUpdate);
}
