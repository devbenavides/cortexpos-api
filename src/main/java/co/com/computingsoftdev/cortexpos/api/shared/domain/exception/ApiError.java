package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    @Builder.Default
    private final Instant timestamp = Instant.now();
    private final Number status;
    private final String code;
    private final String message;
    private final String path;
    private final Map<String, String> details;
}
