package co.com.computingsoftdev.cortexpos.api.person.application;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.*;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.out.PersonRepositoryPort;
import co.com.computingsoftdev.cortexpos.api.person.domain.exception.DuplicateDocumentNumberException;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService implements
        CreatePersonUseCase,
        GetAllPersonUseCase,
        GetPersonByUuidUseCase,
        UpdatePersonUseCase,
        DeletePersonUseCase
{
    private final PersonRepositoryPort personRepositoryPort;

    @Override
    public Person create(Person person) {
        if (personRepositoryPort.existsByDocumentNumber(person.getDocumentNumber())) {
            throw new DuplicateDocumentNumberException("Ya existe una persona registrada con ese número de documento." + person.getDocumentNumber());
        }
        return personRepositoryPort.save(person);
    }

    @Override
    public List<Person> getAll() {
        return personRepositoryPort.findAll();
    }

    @Override
    public Person getByUuid(UUID uuid) {
        return personRepositoryPort.findByUuid(uuid)
                .orElseThrow(()->new NotFoundException("person "+uuid));
    }

    @Override
    public Person update(UUID uuid, Person personToUpdate) {
        Person existingPerson = getByUuid(uuid);
        existingPerson.updateData(
                personToUpdate.getFirstName(),
                personToUpdate.getLastName(),
                personToUpdate.getDocumentType(),
                personToUpdate.getDocumentNumber(),
                personToUpdate.getPhone(),
                personToUpdate.getAddress()
        );
        return personRepositoryPort.save(existingPerson);
    }


    @Override
    @Transactional
    public void delete(UUID uuid) {
        if (!personRepositoryPort.existsByUuid(uuid)) {
            throw new NotFoundException(" person "+uuid);
        }
        personRepositoryPort.deleteByUuid(uuid);
    }
}
