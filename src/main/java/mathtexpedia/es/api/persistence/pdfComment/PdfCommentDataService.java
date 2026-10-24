package mathtexpedia.es.api.persistence.pdfComment;

import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface PdfCommentDataService {

    PdfComment create(PdfComment comment);

    Optional<PdfComment> findById(long id);
    List<PdfComment> findByUserAndPdf(long userId, long pdfId);
    Optional<PdfComment> findOldestPending();
    List<PdfComment> findByStatuses(Collection<CommentStatus> statuses, int offset, int limit);
    List<PdfComment> findVisibleRoots(long pdfId, int offset, int limit);
    List<PdfComment> findPublishedReplies(long rootId, int offset, int limit);

    long countByUserAndStatus(long userId, CommentStatus status);
    long countVisibleRoots(long pdfId);
    Map<Long, Long> countPublishedReplies(Collection<Long> rootIds);
    long countByStatuses(Collection<CommentStatus> statuses);

    int deleteStale(Instant rejectedBefore);

    boolean publish(long id, Set<CommentStatus> from, Instant now);
    boolean reject(long id, Set<CommentStatus> from, ModerationCategory category, Instant now);
    boolean sendToReview(long id, Set<CommentStatus> from, ModerationCategory hint, Instant now);
    boolean hide(long id);
    void markDeleted(long id, Instant now);
    int incrementModerationAttempts(long id);
}
