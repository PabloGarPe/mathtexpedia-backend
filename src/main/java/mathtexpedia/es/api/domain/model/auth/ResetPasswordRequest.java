package mathtexpedia.es.api.domain.model.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    private String password;
}
