package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.event.CommentPublishedEvent;
import mathtexpedia.es.api.domain.event.CommentRejectedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdfComment.AdminCommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.AdminCommentPageDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationStatusDto;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import mathtexpedia.es.api.persistence.pdfCommentReport.PdfCommentReportDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class PdfCommentAdminServiceImpl implements PdfCommentAdminService {

    private static final Logger logger = LoggerFactory.getLogger(PdfCommentAdminServiceImpl.class);

    private static final int MAX_PAGE_SIZE = 50;

    private final PdfCommentDataService pdfCommentDataService;
    private final PdfCommentReportDataService reportDataService;
    private final PdfCommentMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ModerationAvailability availability;

    public PdfCommentAdminServiceImpl(PdfCommentDataService pdfCommentDataService,
                                      PdfCommentReportDataService reportDataService,
                                      PdfCommentMapper mapper,
                                      ApplicationEventPublisher eventPublisher,
                                      ModerationAvailability availability) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.reportDataService = reportDataService;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.availability = availability;
    }

    @Override
    public AdminCommentPageDto getQueue(Set<CommentStatus> statuses, int page, int size) throws MathtexpediaInvalidException {
        logger.info("Fetching moderation queue for {} (page {}, size {})", statuses, page, size);

        if (statuses == null || statuses.isEmpty() || !CommentTransitions.ADMIN_QUEUE.containsAll(statuses))
            throw new MathtexpediaInvalidException("Queue only supports " + CommentTransitions.ADMIN_QUEUE);

        int safePage = Math.max(0, page);
        int safeSize = Math.clamp(size, 1, MAX_PAGE_SIZE);
        int offset = safePage * safeSize;

        List<PdfComment> comments = pdfCommentDataService.findByStatuses(statuses, offset, safeSize);
        long total = pdfCommentDataService.countByStatuses(statuses);
        var reports = reportDataService.countOpenByReason(comments.stream().map(PdfComment::getId).toList());

        List<AdminCommentDto> items = comments.stream()
                .map(comment -> mapper.toAdminDto(comment, reports.get(comment.getId())))
                .toList();

        return new AdminCommentPageDto(items, safePage, safeSize, total, offset + items.size() < total);
    }

    @Override
    public void approve(long commentId) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Admin approving comment {}", commentId);

        PdfComment comment = find(commentId);
        boolean firstPublication = comment.getPublishedAt() == null;

        if (!pdfCommentDataService.publish(commentId, CommentTransitions.APPROVABLE_BY_ADMIN, Instant.now()))
            throw new MathtexpediaConflictException("Comment " + commentId + " cannot be approved from " + comment.getStatus());

        reportDataService.resolveAll(commentId);
        // Restaurar un comentario que ya se publicó antes no vuelve a notificar a nadie
        if (firstPublication)
            eventPublisher.publishEvent(new CommentPublishedEvent(commentId));
    }

    @Override
    public void reject(long commentId, ModerationCategory category) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Admin rejecting comment {} with category {}", commentId, category);

        PdfComment comment = find(commentId);

        if (!pdfCommentDataService.reject(commentId, CommentTransitions.REJECTABLE_BY_ADMIN, category, Instant.now()))
            throw new MathtexpediaConflictException("Comment " + commentId + " cannot be rejected from " + comment.getStatus());

        reportDataService.resolveAll(commentId);
        eventPublisher.publishEvent(new CommentRejectedEvent(commentId));
    }

    private PdfComment find(long commentId) throws MathtexpediaNotFoundException {
        return pdfCommentDataService.findById(commentId)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Comment not found with id: " + commentId));
    }

    @Override
    public ModerationStatusDto getModerationStatus() {
        ModerationAvailability.Status status = availability.status();
        return new ModerationStatusDto(
                status.available(),
                status.since(),
                status.nextCheckAt(),
                pdfCommentDataService.countByStatuses(Set.of(CommentStatus.PENDING)),
                pdfCommentDataService.countByStatuses(Set.of(CommentStatus.REVIEW)));
    }
}
