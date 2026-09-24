package co.com.computingsoftdev.cortexpos.api.person.infrastructure.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "people")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, insertable = false)
    private UUID uuid;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    // Controlado por DEFAULT CURRENT_TIMESTAMP de la BD al insertar
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Controlado por DEFAULT CURRENT_TIMESTAMP de la BD
    @Column(name = "synced_at", insertable = false)
    private OffsetDateTime syncedAt;
}
