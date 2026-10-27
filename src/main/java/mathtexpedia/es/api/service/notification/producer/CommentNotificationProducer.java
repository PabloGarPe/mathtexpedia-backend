package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.CommentNeedsReviewEvent;
import mathtexpedia.es.api.domain.event.CommentPublishedEvent;
import mathtexpedia.es.api.domain.event.CommentRejectedEvent;
import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;
import mathtexpedia.es.api.persistence.pdfComment.PdfCommentDataService;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.user.UserAccountDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CommentNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(CommentNotificationProducer.class);

    private final PdfCommentDataService pdfCommentDataService;
    private final UserAccountDataService userAccountDataService;
    private final NotificationDispatcher dispatcher;

    public CommentNotificationProducer(PdfCommentDataService pdfCommentDataService,
                                       UserAccountDataService userAccountDataService,
                                       NotificationDispatcher dispatcher) {
        this.pdfCommentDataService = pdfCommentDataService;
        this.userAccountDataService = userAccountDataService;
        this.dispatcher = dispatcher;
    }

    /** Una respuesta avisa a quien escribió el comentario contestado; un comentario raíz, a los autores del PDF. */
    @NotificationListener
    public void onCommentPublished(CommentPublishedEvent event) {
        Optional<PdfComment> found = find(event.commentId(), CommentStatus.PUBLISHED);
        if (found.isEmpty())
            return;
        PdfComment comment = found.get();
        PDF pdf = comment.getPdf();
        String actor = comment.getUserAccount().getExternalId();

        if (comment.getReplyTo() != null) {
            dispatcher.dispatch(request(NotificationType.COMMENT_REPLY, comment,
                    Audience.users(Set.of(comment.getReplyTo().getUserAccount().getId())), actor, payload(comment)));
            return;
        }

        Set<Long> pdfAuthorIds = pdf.getAuthors().stream().map(UserAccount::getId).collect(Collectors.toSet());
        dispatcher.dispatch(request(NotificationType.COMMENT_ON_PDF, comment, Audience.users(pdfAuthorIds), actor, payload(comment)));
    }

    @NotificationListener
    public void onCommentRejected(CommentRejectedEvent event) {
        Optional<PdfComment> found = find(event.commentId(), CommentStatus.REJECTED);
        if (found.isEmpty())
            return;
        PdfComment comment = found.get();

        Map<String, Object> payload = payload(comment);
        ModerationCategory category = comment.getModerationCategory();
        payload.put("category", (category == null ? ModerationCategory.OTHER : category).name());

        // Sin actor: el autor es quien debe recibirlo
        dispatcher.dispatch(request(NotificationType.COMMENT_REJECTED, comment,
                Audience.users(Set.of(comment.getUserAccount().getId())), null, payload));
    }

    @NotificationListener
    public void onCommentNeedsReview(CommentNeedsReviewEvent event) {
        Optional<PdfComment> found = pdfCommentDataService.findById(event.commentId());
        if (found.isEmpty() || (found.get().getStatus() != CommentStatus.REVIEW && found.get().getStatus() != CommentStatus.HIDDEN)) {
            logger.debug("Comment {} no longer needs review, skipping notifications", event.commentId());
            return;
        }
        PdfComment comment = found.get();

        List<Long> adminIds = userAccountDataService.getAdminIds();
        if (adminIds.isEmpty()) {
            logger.warn("Comment {} needs review but there are no known admins to notify", comment.getId());
            return;
        }

        Map<String, Object> payload = payload(comment);
        payload.put("pdfId", comment.getPdf().getId());
        payload.put("status", comment.getStatus().name());

        // Tipo sin recurso: el front lleva a la cola de moderación, no a un PDF
        dispatcher.dispatch(new NotificationRequest(NotificationType.COMMENT_REVIEW_PENDING, null, null, payload,
                Audience.users(Set.copyOf(adminIds)), null, Set.of()));
    }

    private Optional<PdfComment> find(long commentId, CommentStatus expected) {
        Optional<PdfComment> found = pdfCommentDataService.findById(commentId).filter(c -> c.getStatus() == expected);
        if (found.isEmpty())
            logger.debug("Comment {} is no longer {}, skipping notifications", commentId, expected);
        return found;
    }

    private NotificationRequest request(NotificationType type, PdfComment comment, Audience audience, String actorExternalId,
                                        Map<String, Object> payload) {
        PDF pdf = comment.getPdf();
        return new NotificationRequest(type, pdf.getId(), pdf.getSubject().getId(), payload, audience, actorExternalId, Set.of());
    }

    private Map<String, Object> payload(PdfComment comment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("commentId", comment.getId());
        payload.put("pdfName", comment.getPdf().getName());
        payload.put("username", comment.getUserAccount().getUsername());
        return payload;
    }
}
