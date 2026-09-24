package co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        String documentType,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 50)
        String documentNumber,

        String phone,
        String address
) {
}
