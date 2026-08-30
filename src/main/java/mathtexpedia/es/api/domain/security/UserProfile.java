package mathtexpedia.es.api.domain.security;

import lombok.Getter;

@Getter
public class UserProfile {
    private final String email;
    private final UserRole role;
    private final String id;

    public UserProfile(String email, String role, String id) {
        this.email = email;
        this.id = id;
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
