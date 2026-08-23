package mathtexpedia.es.api.domain.security;

import lombok.Getter;

@Getter
public class UserProfile {
    private final String email;
    private final UserRole role;

    public UserProfile(String email, String role) {
        this.email = email;
        if (role.equalsIgnoreCase("ROLE_ADMIN")) {
            this.role = UserRole.ADMIN;
        } else {
            this.role = UserRole.USER;
        }
    }
}
