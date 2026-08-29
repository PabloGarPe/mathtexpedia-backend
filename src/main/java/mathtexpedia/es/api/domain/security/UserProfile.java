package mathtexpedia.es.api.domain.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserProfile {

    @Schema(description = "Email del usuario, extraído del JWT")
    private final String email;

    @Schema(description = "Rol del usuario")
    private final UserRole role;

    public UserProfile(String email, String role) {
        this.email = email;
        if (role.equalsIgnoreCase("ROLE_ADMIN")) {
            this.role = UserRole.ADMIN;
        } else {
            this.role = UserRole.USER;
        }
    }

    @Override
    public String toString() {
        return "[ User: " +
                email +
                " with Role: " +
                role +
                "]";
    }
}
