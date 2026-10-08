package co.com.computingsoftdev.cortexpos.api.user.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    /** Carga usuario + persona + roles + permisos en una sola consulta. */
    @EntityGraph(attributePaths = {"person", "roles", "roles.permissions"})
    @Query("""
            select u from UserJpaEntity u
            where lower(u.username) = lower(:login) or lower(u.email) = lower(:login)
            """)
    Optional<UserJpaEntity> findByLogin(@Param("login") String login);

    @EntityGraph(attributePaths = {"person", "roles", "roles.permissions"})
    Optional<UserJpaEntity> findByUuid(UUID uuid);

    @Modifying
    @Query("update UserJpaEntity u set u.lastLoginAt = :at where u.uuid = :uuid")
    int updateLastLogin(@Param("uuid") UUID uuid, @Param("at") Instant at);
}
