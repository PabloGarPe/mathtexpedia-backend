package mathtexpedia.es.api.domain.model.userAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserAccountDto {

    @Schema(description = "Identificador interno del usuario", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Email del usuario", example = "user@example.com")
    private String email;

    @Schema(description = "Nombre del usuario", example = "John Doe")
    private String name;

    @Schema(description = "Estado del usuario", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Fecha de creación del usuario", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(description = "Fecha del último acceso del usuario")
    private Instant lastSeenAt;

    @Schema(description = "Fecha de finalización del perfil del usuario")
    private Instant profileCompletedAt;
}
