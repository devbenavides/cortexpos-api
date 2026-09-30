package co.com.computingsoftdev.cortexpos.api.person.domain.ports.out;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepositoryPort {
    Person save(Person person);
    List<Person> findAll();
    Optional<Person> findById(Long id);
    Optional<Person> findByUuid(UUID uuid);
    Optional<Person> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByUuid(UUID uuid);
    void deleteByUuid(UUID uuid);
}
