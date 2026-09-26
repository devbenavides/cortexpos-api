package co.com.computingsoftdev.cortexpos.api.person.infrastructure.out.persistence;

import co.com.computingsoftdev.cortexpos.api.person.domain.model.Person;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    Person toDomain(PersonJpaEntity entity);

    PersonJpaEntity toEntity(Person domain);

    List<Person> toDomainList(List<PersonJpaEntity> entities);
}
