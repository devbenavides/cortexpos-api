package co.com.computingsoftdev.cortexpos.api.person.infrastructure.in.web.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonResponse(
        Long id,
        UUID uuid,
        String firstName,
        String lastName,
        String fullName,
        String documentType,
        String documentNumber,
        String phone,
        String address,
        OffsetDateTime createdAt
) {
}
