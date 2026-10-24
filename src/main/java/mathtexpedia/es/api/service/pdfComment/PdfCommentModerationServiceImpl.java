package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.event.CommentNeedsReviewEvent;
import mathtexpedia.es.api.domain.event.CommentPublishedEvent;
import mathtexpedia.es.api.domain.event.CommentRejectedEvent;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PdfCommentModerationServiceImpl implements PdfCommentModerationService {

    private static final Logger logger = LoggerFactory.getLogger(PdfCommentModerationServiceImpl.class);

    private final PdfCommentDataService pdfCommentDataService;
    private final ApplicationEventPublisher eventPublisher;
    private final int maxAttempts;

    public PdfCommentModerationServiceImpl(PdfCommentDataService pdfCommentDataService,
                                           ApplicationEventPublisher eventPublisher,
                                           @Value("${moderation.max-attempts}") int maxAttempts) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.eventPublisher = eventPublisher;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public Optional<PdfComment> nextPending() {
        return pdfCommentDataService.findOldestPending();
    }

    @Override
    public boolean publish(long commentId) {
        logger.info("AI verdict OK for comment {}", commentId);

        boolean changed = pdfCommentDataService.publish(commentId, CommentTransitions.BY_AI, Instant.now());
        if (changed)
            eventPublisher.publishEvent(new CommentPublishedEvent(commentId));
        return changed;
    }

    @Override
    public boolean reject(long commentId, ModerationCategory category) {
        logger.info("AI verdict REJECT ({}) for comment {}", category, commentId);

        boolean changed = pdfCommentDataService.reject(commentId, CommentTransitions.BY_AI, category, Instant.now());
        if (changed)
            eventPublisher.publishEvent(new CommentRejectedEvent(commentId));
        return changed;
    }

    @Override
    public boolean sendToReview(long commentId, ModerationCategory hint) {
        logger.info("Comment {} sent to admin review (hint {})", commentId, hint);

        boolean changed = pdfCommentDataService.sendToReview(commentId, CommentTransitions.BY_AI, hint, Instant.now());
        if (changed)
            eventPublisher.publishEvent(new CommentNeedsReviewEvent(commentId));
        return changed;
    }

    @Override
    public boolean registerFailedAttempt(long commentId) {
        int attempts = pdfCommentDataService.incrementModerationAttempts(commentId);
        logger.warn("Moderation attempt {} of {} failed for comment {}", attempts, maxAttempts, commentId);

        return attempts >= maxAttempts && sendToReview(commentId, null);
    }
}
