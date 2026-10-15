package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.SubjectCreatedEvent;
import mathtexpedia.es.api.domain.event.SubjectUnitCreatedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** Los temas nuevos solo se notifican a los suscritos a la asignatura; las asignaturas nuevas, a todos. */
@Component
public class SubjectNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(SubjectNotificationProducer.class);

    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final NotificationDispatcher dispatcher;

    public SubjectNotificationProducer(SubjectDataService subjectDataService, SubjectUnitDataService subjectUnitDataService,
                                       NotificationDispatcher dispatcher) {
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
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

    @NotificationListener
    public void onSubjectUnitCreated(SubjectUnitCreatedEvent event) {
        subjectUnitDataService.getById(event.subjectUnitId()).ifPresentOrElse(
                unit -> dispatcher.dispatch(new NotificationRequest(NotificationType.SUBJECT_UNIT_CREATED,
                        unit.getSubject().getId(), unit.getSubject().getId(),
                        Map.of("subjectName", unit.getSubject().getName(), "subjectUnitName", unit.getName()),
                        Audience.subjectSubscribers(unit.getSubject().getId()), event.actorExternalId(), Set.of())),
                () -> logger.debug("Subject unit {} no longer exists, skipping notifications", event.subjectUnitId()));
    }
}
