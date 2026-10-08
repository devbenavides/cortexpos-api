package co.com.computingsoftdev.cortexpos.api.user.infrastructure.out.persistence;

import co.com.computingsoftdev.cortexpos.api.user.domain.model.Role;
import co.com.computingsoftdev.cortexpos.api.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPersistenceMapper {
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    User toDomain(UserJpaEntity entity);

    Role toDomain(RoleJpaEntity entity);

    /** Los permisos viajan al dominio como simples nombres. */
    default String toPermissionName(PermissionJpaEntity permission) {
        return permission.getName();
    }
}
