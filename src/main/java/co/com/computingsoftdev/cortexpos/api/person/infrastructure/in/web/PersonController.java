package co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.CreatePersonUseCase;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.GetAllPersonUseCase;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.GetPersonByUuidUseCase;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.UpdatePersonUseCase;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.request.PersonCreateRequest;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.request.PersonUpdateRequest;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.response.PersonResponse;
import co.com.computingsoftdev.cortexpos.api.shared.infrastructure.validation.ObjectsValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {
    private final CreatePersonUseCase createPersonUseCase;
    private final GetAllPersonUseCase getAllPersonUseCase;
    private final GetPersonByUuidUseCase getPersonByUuidUseCase;
    private final UpdatePersonUseCase updatePersonUseCase;

    private final PersonWebMapper webMapper;
    private final ObjectsValidator validator;

    @PostMapping
    public ResponseEntity<PersonResponse> create(@RequestBody PersonCreateRequest request){        // 1. DTO Request -> Modelo de Dominio
        // 1. Validación sintáctica/formato (Throw ConstraintViolationException en fallo)
        validator.validate(request);

        // 2. DTO -> Modelo de Dominio
        Person domainModel = webMapper.toDomain(request);

        // 3. Ejecución del Caso de Uso (Lógica pura de negocio)
        Person createdPerson = createPersonUseCase.create(domainModel);

        // 4. Modelo de Dominio -> DTO Response
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(createdPerson));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> getAll(){
        List<PersonResponse> response = webMapper.toResponseList(getAllPersonUseCase.getAll());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PersonResponse> getByUuid(@PathVariable UUID uuid){
        Person person = getPersonByUuidUseCase.getByUuid(uuid);

        return ResponseEntity.ok(webMapper.toResponse(person));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<PersonResponse> update(@PathVariable UUID uuid, @RequestBody PersonUpdateRequest request){
        validator.validate(request);

        Person personToUpdate = webMapper.toDomain(request);
        Person updatePerson = updatePersonUseCase.update(uuid,personToUpdate);

        return ResponseEntity.ok(webMapper.toResponse(updatePerson));
    }

}
