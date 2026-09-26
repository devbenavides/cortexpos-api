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

    //Metodo explícito para la lógica de actualización
    public void updateData(String firstName, String lastName, String documentType,
                           String documentNumber, String phone, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.address = address;
        this.syncedAt = OffsetDateTime.now(); // Lógica protegida en el núcleo
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}

