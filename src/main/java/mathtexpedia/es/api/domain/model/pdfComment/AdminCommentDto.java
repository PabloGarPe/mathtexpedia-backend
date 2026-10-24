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
    private Long parentId;
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
    private Instant publishedAt;
    private Instant moderatedAt;
}
