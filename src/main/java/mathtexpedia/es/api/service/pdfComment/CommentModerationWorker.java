package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.exception.ModerationTimeoutException;
import mathtexpedia.es.api.domain.exception.ModerationUnavailableException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;
import mathtexpedia.es.api.domain.port.moderation.CommentModerationPort;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentModerationWorker {

    private static final Logger logger = LoggerFactory.getLogger(CommentModerationWorker.class);

    private final ModerationAvailability availability;
    private final CommentModerationPort port;
    private final PdfCommentModerationService moderation;

    public CommentModerationWorker(ModerationAvailability moderationAvailability, CommentModerationPort port, PdfCommentModerationService pdfCommentModerationService) {
        this.availability = moderationAvailability;
        this.port = port;
        this.moderation = pdfCommentModerationService;
    }

    @Scheduled(fixedDelayString = "${moderation.worker.poll-interval-ms}")
    public void run() {
        if (!availability.canAttempt()) return;

        try {
            drainQueue();
        } catch (RuntimeException e) {
            logger.error("Unexpected error in the moderation worker", e);
        }
    }

    private void drainQueue() {
        while (true) {
            Optional<PdfComment> next = moderation.nextPending();
            if (next.isEmpty()) break;

            long id = next.get().getId();
            try {
                apply(id, port.moderate(next.get().getBody()));
            } catch (ModerationTimeoutException e) {
                moderation.registerFailedAttempt(id);
                return;
            } catch (ModerationUnavailableException e) {
                return;
            } catch (ModerationInvalidOutputException e) {
                logger.warn("Invalid moderation output for comment {}: {}", id, e.getMessage());
                moderation.registerFailedAttempt(id);
            }
        }
    }

    private void apply(long commentId, ModerationVerdict verdict) {
        switch (verdict.decision()) {
            case OK -> moderation.publish(commentId);
            case REVIEW -> moderation.sendToReview(commentId, verdict.category());
            case REJECT -> moderation.reject(commentId, verdict.category());
        }
    }
}
