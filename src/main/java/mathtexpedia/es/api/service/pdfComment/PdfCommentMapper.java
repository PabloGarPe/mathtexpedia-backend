package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.model.pdfComment.AdminCommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.userAccount.UserAccountMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** Todos los comentarios llegan con autor, PDF y comentario respondido ya cargados (ver {@code PdfCommentDataService}). */
@Component
public class PdfCommentMapper {

    private final UserAccountMapper userAccountMapper;

    public PdfCommentMapper(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    /**
     * Para el listado público. Una raíz que ya no está publicada se devuelve como "no disponible", sin texto ni autor.
     *
     * @param replyCount respuestas publicadas (solo para raíces; nulo en respuestas)
     * @param likeCount  likes del comentario
     */
    public CommentDto toPublicDto(PdfComment comment, Set<Long> pdfAuthorIds, Long replyCount, long likeCount) {
        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            return CommentDto.builder()
                    .id(comment.getId())
                    .pdfId(comment.getPdf().getId())
                    .available(false)
                    .replyCount(replyCount)
                    .createdAt(comment.getCreatedAt())
                    .publishedAt(comment.getPublishedAt())
                    .build();
        }
        return base(comment, pdfAuthorIds).status(CommentStatus.PUBLISHED).available(true).replyCount(replyCount)
                .likeCount(likeCount).build();
    }

    /** Para el propio autor: incluye el estado y el motivo del rechazo. */
    public CommentDto toOwnDto(PdfComment comment, Set<Long> pdfAuthorIds, long likeCount) {
        return base(comment, pdfAuthorIds)
                .likeCount(likeCount)
                .status(comment.getStatus())
                .moderationCategory(comment.getModerationCategory())
                .available(true)
                .build();
    }

    public AdminCommentDto toAdminDto(PdfComment comment, Map<ReportReason, Long> openReports) {
        Map<ReportReason, Long> reasons = openReports == null ? Map.of() : openReports;
        UserAccount author = comment.getUserAccount();

        return AdminCommentDto.builder()
                .id(comment.getId())
                .pdfId(comment.getPdf().getId())
                .pdfName(comment.getPdf().getName())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .replyToId(comment.getReplyTo() == null ? null : comment.getReplyTo().getId())
                .author(userAccountMapper.toAuthorDto(author))
                .authorEmail(author.getEmail())
                .body(comment.getBody())
                .status(comment.getStatus())
                .moderationCategory(comment.getModerationCategory())
                .reportCount(reasons.values().stream().mapToLong(Long::longValue).sum())
                .reportReasons(reasons)
                .createdAt(comment.getCreatedAt())
                .publishedAt(comment.getPublishedAt())
                .moderatedAt(comment.getModeratedAt())
                .build();
    }

    private CommentDto.CommentDtoBuilder base(PdfComment comment, Set<Long> pdfAuthorIds) {
        UserAccount author = comment.getUserAccount();
        PdfComment replyTo = comment.getReplyTo();

        return CommentDto.builder()
                .id(comment.getId())
                .pdfId(comment.getPdf().getId())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .replyToId(replyTo == null ? null : replyTo.getId())
                // Solo se nombra al autor del comentario respondido mientras ese comentario siga publicado
                .replyToUsername(replyTo != null && replyTo.getStatus() == CommentStatus.PUBLISHED
                        ? replyTo.getUserAccount().getUsername() : null)
                .author(userAccountMapper.toAuthorDto(author))
                .byPdfAuthor(pdfAuthorIds.contains(author.getId()))
                .body(comment.getBody())
                .createdAt(comment.getCreatedAt())
                .publishedAt(comment.getPublishedAt());
    }
}
