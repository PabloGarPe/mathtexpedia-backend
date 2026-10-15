package mathtexpedia.es.api.domain.model.userAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/** Datos públicos de un usuario cuando aparece como autor de algo. No incluye el email. */
@Data
@AllArgsConstructor
public class AuthorDto {

    @Schema(description = "Identificador interno del usuario; sirve para pedir su foto en GET /users/{id}/avatar", example = "42")
    private Long id;

    @Schema(description = "Nombre público. Nulo si el usuario aún no ha completado su perfil", nullable = true, example = "ada_lovelace")
    private String username;

    @Schema(description = "Si tiene foto de perfil")
    private boolean hasAvatar;

    @Schema(description = "Versión de la foto (cuándo se cambió); clave de caché. Nulo si no tiene", nullable = true)
    private Instant avatarUpdatedAt;
}
