package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.PdfAuthorsAddedEvent;
import mathtexpedia.es.api.domain.event.PdfCreatedEvent;
import mathtexpedia.es.api.domain.event.PdfFileReplacedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class PdfNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(PdfNotificationProducer.class);

    private final PDFDataService pdfDataService;
    private final NotificationDispatcher dispatcher;

    public PdfNotificationProducer(PDFDataService pdfDataService, NotificationDispatcher dispatcher) {
        this.pdfDataService = pdfDataService;
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onPdfCreated(PdfCreatedEvent event) {
        Optional<PDF> found = pdfDataService.getPDFById(event.pdfId());
        if (found.isEmpty()) {
            logger.debug("PDF {} no longer exists, skipping notifications", event.pdfId());
            return;
        }
        PDF pdf = found.get();
        Set<Long> authorIds = Set.copyOf(event.authorIds());

        // Los autores reciben la notificación personal, no la general
        dispatcher.dispatch(new NotificationRequest(NotificationType.PDF_CREATED, pdf.getId(), pdf.getSubject().getId(),
                payload(pdf), Audience.all(), event.actorExternalId(), authorIds));

        if (!authorIds.isEmpty())
            dispatcher.dispatch(authorAdded(pdf, authorIds, event.actorExternalId()));
    }

    @NotificationListener
    public void onPdfFileReplaced(PdfFileReplacedEvent event) {
        pdfDataService.getPDFById(event.pdfId()).ifPresentOrElse(
                pdf -> dispatcher.dispatch(new NotificationRequest(NotificationType.PDF_FILE_UPDATED, pdf.getId(),
                        pdf.getSubject().getId(), payload(pdf), Audience.all(), event.actorExternalId(), Set.of())),
                () -> logger.debug("PDF {} no longer exists, skipping notifications", event.pdfId()));
    }

    @NotificationListener
    public void onPdfAuthorsAdded(PdfAuthorsAddedEvent event) {
        if (event.addedAuthorIds().isEmpty())
            return;

        pdfDataService.getPDFById(event.pdfId()).ifPresentOrElse(
                pdf -> dispatcher.dispatch(authorAdded(pdf, Set.copyOf(event.addedAuthorIds()), event.actorExternalId())),
                () -> logger.debug("PDF {} no longer exists, skipping notifications", event.pdfId()));
    }

    private NotificationRequest authorAdded(PDF pdf, Set<Long> authorIds, String actorExternalId) {
        return new NotificationRequest(NotificationType.PDF_AUTHOR_ADDED, pdf.getId(), pdf.getSubject().getId(),
                payload(pdf), Audience.users(authorIds), actorExternalId, Set.of());
    }

    private Map<String, Object> payload(PDF pdf) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pdfName", pdf.getName());
        payload.put("subjectName", pdf.getSubject().getName());
        if (pdf.getSubjectUnit() != null)
            payload.put("subjectUnitName", pdf.getSubjectUnit().getName());
        return payload;
    }
}
