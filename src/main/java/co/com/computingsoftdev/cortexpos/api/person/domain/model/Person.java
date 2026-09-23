package co.com.computingsoftdev.cortexpos.api.person.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter

public class Person {
    private Long id;
    private UUID uuid;
    private String firstName;
    private String lastName;
    private String documentType;
    private String documentNumber;
    private String phone;
    private String address;
    private OffsetDateTime createdAt;
    private OffsetDateTime syncedAt;

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}

