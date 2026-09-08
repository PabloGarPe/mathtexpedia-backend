package mathtexpedia.es.api.infrastructure.adapter.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakRoleRepresentation {
    private String id;
    private String name;
}