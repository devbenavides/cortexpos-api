package co.com.computingsoftdev.cortexpos.api.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class Role {

    private final Long id;
    private final String name;
    private final boolean active;

    @Builder.Default
    private final Set<String> permissions = Set.of();
}
