package co.com.computingsoftdev.cortexpos.api.person.infrastructure.out.persistence;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.out.PersonRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Component
@RequiredArgsConstructor
public class PersonPersistenceAdapter implements PersonRepositoryPort {

    private final SpringDataPersonRepository repository;
    private final PersonMapper mapper;

    @Override
    public Person save(Person person) {
        PersonJpaEntity entity = mapper.toEntity(person);
        PersonJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<Person> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<Person> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Person> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Person> findByDocumentNumber(String documentNumber) {
        return repository.findByDocumentNumber(documentNumber).map(mapper::toDomain);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }
}
