package co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.request.PersonCreateRequest;
import co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.response.PersonResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonWebMapper  {
    Person toDomain(PersonCreateRequest request);

    @Mapping(target = "fullName", expression = "java(person.getFullName())")
    PersonResponse toResponse(Person person);
}
