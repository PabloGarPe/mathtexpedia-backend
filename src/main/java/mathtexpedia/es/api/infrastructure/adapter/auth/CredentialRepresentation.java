package mathtexpedia.es.api.infrastructure.adapter.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CredentialRepresentation {
    private String type;
    private String value;
    private boolean temporary;
}