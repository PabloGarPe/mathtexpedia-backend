package mathtexpedia.es.api.domain.model.quizAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class LeaderboardEntryDto {

    @Schema(description = "Identificador interno del usuario; sirve para pedir su foto en GET /users/{id}/avatar")
    private Long userId;

    @Schema(description = "Nombre público (username) del usuario. Nulo si aún no ha completado su perfil", nullable = true, example = "ada_lovelace")
    private String username;

    @Schema(description = "Si el usuario tiene foto de perfil")
    private boolean hasAvatar;

    @Schema(description = "Versión de la foto (cuándo se cambió); clave de caché. Nulo si no tiene", nullable = true)
    private Instant avatarUpdatedAt;

    private Double bestAdjustedScore;
    private int rank;

}
