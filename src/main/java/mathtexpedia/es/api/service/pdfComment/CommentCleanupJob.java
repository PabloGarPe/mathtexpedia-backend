package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Retención de comentarios: los rechazados caducan a los N días y los borrados se eliminan, siempre que no tengan respuestas. */
@Component
public class CommentCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(CommentCleanupJob.class);

    private final PdfCommentDataService pdfCommentDataService;
    private final long rejectedRetentionDays;

    public CommentCleanupJob(PdfCommentDataService pdfCommentDataService,
                             @Value("${comments.rejected-retention-days}") long rejectedRetentionDays) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.rejectedRetentionDays = rejectedRetentionDays;
    }

    @Scheduled(cron = "${comments.cleanup.cron}")
    public void cleanup() {
        try {
            int deleted = pdfCommentDataService.deleteStale(Instant.now().minus(Duration.ofDays(rejectedRetentionDays)));
            logger.info("Comment cleanup: {} comments deleted", deleted);
        } catch (RuntimeException e) {
            logger.error("Error cleaning up comments", e);
        }
    }
}
