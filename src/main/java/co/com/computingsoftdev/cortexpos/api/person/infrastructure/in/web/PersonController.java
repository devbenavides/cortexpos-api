package co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.domain.ports.in.CreatePersonUseCase;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.request.PersonCreateRequest;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.response.PersonResponse;
import co.com.computingsoftdev.cortexpos.api.shared.infrastructure.validation.ObjectsValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {
    private final CreatePersonUseCase createPersonUseCase;
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

}
