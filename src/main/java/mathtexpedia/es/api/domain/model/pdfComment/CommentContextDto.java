package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentContextDto(
        @Schema(description = "Raíz del hilo, con 'replyCount'. Con 'available' a false si ya no está publicada pero conserva respuestas")
        CommentDto root,
        @Schema(description = "El comentario pedido. Es el mismo que 'root' si el comentario es una raíz")
        CommentDto target) {
}
