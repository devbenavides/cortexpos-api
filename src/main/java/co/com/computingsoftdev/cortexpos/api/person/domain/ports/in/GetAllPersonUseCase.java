package co.com.computingsoftdev.cortexpos.api.person.domain.ports.in;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;

import java.util.List;

public interface GetAllPersonUseCase {
    List<Person> getAll();
}
