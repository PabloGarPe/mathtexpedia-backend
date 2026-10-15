package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.SubjectCreatedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * No hay listener de {@code SubjectUnitCreatedEvent} a propósito: los temas nuevos solo se notificarán a los
 * suscritos a la asignatura (#70).
 */
@Component
public class SubjectNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(SubjectNotificationProducer.class);

    private final SubjectDataService subjectDataService;
    private final NotificationDispatcher dispatcher;

    public SubjectNotificationProducer(SubjectDataService subjectDataService, NotificationDispatcher dispatcher) {
        this.subjectDataService = subjectDataService;
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onSubjectCreated(SubjectCreatedEvent event) {
        subjectDataService.getById(event.subjectId()).ifPresentOrElse(
                subject -> dispatcher.dispatch(new NotificationRequest(NotificationType.SUBJECT_CREATED, subject.getId(),
                        subject.getId(), Map.of("subjectName", subject.getName()), Audience.all(),
                        event.actorExternalId(), Set.of())),
                () -> logger.debug("Subject {} no longer exists, skipping notifications", event.subjectId()));
    }
}
