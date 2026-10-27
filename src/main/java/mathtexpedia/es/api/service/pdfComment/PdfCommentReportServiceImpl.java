package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.event.CommentNeedsReviewEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import mathtexpedia.es.api.persistence.pdfCommentReport.PdfCommentReportDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
public class PdfCommentReportServiceImpl implements PdfCommentReportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfCommentReportServiceImpl.class);

    private final PdfCommentDataService pdfCommentDataService;
    private final PdfCommentReportDataService reportDataService;
    private final UserAccountService userAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final int hideThreshold;

    public PdfCommentReportServiceImpl(PdfCommentDataService pdfCommentDataService,
                                       PdfCommentReportDataService reportDataService,
                                       UserAccountService userAccountService,
                                       ApplicationEventPublisher eventPublisher,
                                       @Value("${comments.report-hide-threshold}") int hideThreshold) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.reportDataService = reportDataService;
        this.userAccountService = userAccountService;
        this.eventPublisher = eventPublisher;
        this.hideThreshold = hideThreshold;
    }

    @Override
    public void report(long commentId, ReportReason reason, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaForbiddenException,
            MathtexpediaConflictException {
        logger.info("User {} reporting comment {} for {}", user.getId(), commentId, reason);

        UserAccount account = userAccountService.getOrProvision(user);
        PdfComment comment = pdfCommentDataService.findById(commentId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Comment not found with id: " + commentId));

        if (Objects.equals(comment.getUserAccount().getId(), account.getId()))
            throw new MathtexpediaForbiddenException("User " + user.getId() + " cannot report their own comment");
        if (comment.getStatus() != CommentStatus.PUBLISHED)
            throw new MathtexpediaConflictException("Comment " + commentId + " is not published");

        reportDataService.insertIfAbsent(commentId, account.getId(), reason, Instant.now());

        // Se evalúa también si ya había denunciado: es inofensivo y cubre una carrera con otra denuncia que cruzó el umbral
        if (reportDataService.countOpenReporters(commentId) >= hideThreshold && pdfCommentDataService.hide(commentId)) {
            logger.info("Comment {} hidden after reaching {} reports", commentId, hideThreshold);
            eventPublisher.publishEvent(new CommentNeedsReviewEvent(commentId));
        }
    }
}
