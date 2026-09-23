package co.com.computingsoftdev.cortexpos.api.person.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;

public interface CreatePersonUseCase {
    Person create(Person person);
}
