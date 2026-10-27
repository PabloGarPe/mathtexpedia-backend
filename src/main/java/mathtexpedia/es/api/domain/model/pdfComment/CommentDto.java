package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mathtexpedia.es.api.domain.model.userAccount.AuthorDto;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class CommentDto {

    private Long id;

    private Long pdfId;

    @Schema(description = "Raíz del hilo; nulo si este comentario es una raíz", nullable = true)
    private Long parentId;

    @Schema(description = "Comentario concreto al que se contesta (para pintar 'en respuesta a @usuario'). Nulo en las raíces", nullable = true)
    private Long replyToId;

    @Schema(description = "Usuario del comentario al que se contesta. Nulo si no se contesta a nadie o ese comentario ya no está publicado", nullable = true)
    private String replyToUsername;

    @Schema(description = "Autor del comentario. Nulo si 'available' es false", nullable = true)
    private AuthorDto author;

    @Schema(description = "Si el autor del comentario es también autor del PDF (para una insignia)")
    private boolean byPdfAuthor;

    @Schema(description = "Texto. Nulo si 'available' es false", nullable = true)
    private String body;

    @Schema(description = "Estado. En los listados públicos es siempre PUBLISHED, o nulo si 'available' es false", nullable = true)
    private CommentStatus status;

    @Schema(description = "Motivo del rechazo; solo aparece en los comentarios propios (GET /pdf/{pdfId}/comments/me)", nullable = true)
    private ModerationCategory moderationCategory;

    @Schema(description = "False si es una raíz que ya no está disponible (borrada, oculta o rechazada) pero conserva respuestas publicadas: "
            + "mostrar 'comentario no disponible'")
    private boolean available;

    @Schema(description = "Respuestas publicadas. Solo en las raíces de los listados públicos", nullable = true)
    private Long replyCount;

    @Schema(description = "Likes del comentario. 0 si 'available' es false. Qué comentarios le gustan al usuario: GET /me/comment-likes")
    private long likeCount;

    private Instant createdAt;

    @Schema(description = "Cuándo se aprobó; es el que ordena los listados. Nulo si todavía no se ha publicado", nullable = true)
    private Instant publishedAt;
}
