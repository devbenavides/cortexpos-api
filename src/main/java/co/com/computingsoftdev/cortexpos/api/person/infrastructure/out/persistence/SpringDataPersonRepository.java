package co.com.computingsoftdev.cortexpos.api.person.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPersonRepository extends JpaRepository<PersonJpaEntity, Long> {
    Optional<PersonJpaEntity> findByUuid(UUID uuid);
    Optional<PersonJpaEntity> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByUuid(UUID uuid);
    void deleteByUuid(UUID uuid);
}
