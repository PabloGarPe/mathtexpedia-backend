package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import mathtexpedia.es.api.domain.model.userAccount.AuthorDto;

import java.time.Instant;
import java.util.Map;

/** Comentario tal como lo ve un administrador en la cola de moderación. */
@Data
@Builder
@AllArgsConstructor
public class AdminCommentDto {

    private Long id;
    private Long pdfId;
    private String pdfName;
    @Schema(description = "Raíz del hilo; nulo si es una raíz", nullable = true)
    private Long parentId;
    @Schema(description = "Comentario al que contesta; nulo en las raíces", nullable = true)
    private Long replyToId;
    private AuthorDto author;
    private String authorEmail;
    private String body;
    private CommentStatus status;

    @Schema(description = "Categoría que propuso la IA (REVIEW) o con la que se rechazó", nullable = true)
    private ModerationCategory moderationCategory;

    @Schema(description = "Denuncias abiertas (de usuarios distintos)")
    private long reportCount;

    @Schema(description = "Denuncias abiertas agrupadas por motivo")
    private Map<ReportReason, Long> reportReasons;

    private Instant createdAt;
    @Schema(description = "Cuándo se publicó; nulo si todavía no se ha publicado", nullable = true)
    private Instant publishedAt;
    @Schema(description = "Cuándo lo evaluó la IA o un admin; nulo si todavía no ha pasado por moderación", nullable = true)
    private Instant moderatedAt;
}
