package mathtexpedia.es.api.domain.security;

public enum UserRole {
    USER, ADMIN;

    public static UserRole fromKeycloakRole(String role) {
        return "ROLE_ADMIN".equalsIgnoreCase(role) ? ADMIN : USER;
    }
}