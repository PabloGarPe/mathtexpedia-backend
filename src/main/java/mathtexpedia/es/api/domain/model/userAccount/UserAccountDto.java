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

    @Schema(description = "Nombre público del usuario. Nulo hasta que completa el perfil", nullable = true, example = "ada_lovelace")
    private String username;

    @Schema(description = "Nombre de pila", nullable = true, example = "Ada")
    private String firstName;

    @Schema(description = "Apellidos", nullable = true, example = "Lovelace")
    private String lastName;

    @Schema(description = "Biografía corta", nullable = true)
    private String bio;

    @Schema(description = "Universidad o centro de estudios", nullable = true, example = "Universidad de Sevilla")
    private String university;

    @Schema(description = "Estudios que cursa", nullable = true, example = "Grado en Matemáticas")
    private String studies;

    @Schema(description = "Si el usuario tiene foto de perfil. Se descarga de GET /users/{id}/avatar")
    private boolean hasAvatar;

    @Schema(description = "Cuándo se cambió la foto por última vez; nulo si no tiene. Úsalo como clave de caché de la imagen", nullable = true)
    private Instant avatarUpdatedAt;

    @Schema(description = "Estado del usuario", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Fecha de creación del usuario", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant createdAt;

    @Schema(description = "Fecha del último acceso del usuario")
    private Instant lastSeenAt;

    @Schema(description = "Fecha en que se completó el perfil (se fija al guardar por primera vez un nombre de usuario). "
            + "Nulo = el perfil está por completar y el front debe pedirlo", nullable = true)
    private Instant profileCompletedAt;
}
