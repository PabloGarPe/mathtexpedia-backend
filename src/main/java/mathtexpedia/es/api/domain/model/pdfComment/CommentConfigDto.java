package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentConfigDto(
        @Schema(description = "Longitud máxima de un comentario, en puntos de código Unicode", example = "2000")
        int maxLength,
        @Schema(description = "Comentarios pendientes de revisión que un usuario puede tener a la vez", example = "3")
        int maxPendingPerUser) {
}
