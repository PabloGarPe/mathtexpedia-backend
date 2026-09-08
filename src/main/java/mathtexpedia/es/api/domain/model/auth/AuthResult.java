package mathtexpedia.es.api.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
    private String jwt;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;
}