package mathtexpedia.es.api.domain.model.auth;

import lombok.Builder;
import lombok.Data;
import mathtexpedia.es.api.domain.security.UserRole;

import java.time.Instant;

@Data
@Builder
public class UserDTO {
    private final String id;
    private final String email;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    private final boolean enabled;
    private final Instant createdAt;
}
