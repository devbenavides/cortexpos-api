package co.com.computingsoftdev.cortexpos.api.person.application;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.CreatePersonUseCase;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.out.PersonRepositoryPort;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.DuplicateDocumentNumberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonService implements CreatePersonUseCase {
    private final PersonRepositoryPort personRepositoryPort;

    @Override
    public Person create(Person person) {
        if (personRepositoryPort.existsByDocumentNumber(person.getDocumentNumber())){
            throw new DuplicateDocumentNumberException("Ya existe una persona registrada con ese número de documento.");
        }
        return personRepositoryPort.save(person);
    }
}
