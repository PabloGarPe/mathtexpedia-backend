package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.SubjectCreatedEvent;
import mathtexpedia.es.api.domain.event.SubjectUnitCreatedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubjectDataService;
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

/**
 * Los temas nuevos solo se notifican a los suscritos a la asignatura. Las asignaturas nuevas, a los usuarios de los grados en
 * los que se colocan y a los que no tienen grado; si no se colocan en ningún grado, a todos.
 */
@Component
public class SubjectNotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(SubjectNotificationProducer.class);

    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final DegreeSubjectDataService degreeSubjectDataService;
    private final NotificationDispatcher dispatcher;

    public SubjectNotificationProducer(SubjectDataService subjectDataService, SubjectUnitDataService subjectUnitDataService,
                                       DegreeSubjectDataService degreeSubjectDataService, NotificationDispatcher dispatcher) {
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.degreeSubjectDataService = degreeSubjectDataService;
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onSubjectCreated(SubjectCreatedEvent event) {
        subjectDataService.getById(event.subjectId()).ifPresentOrElse(
                subject -> {
                    // Se lee de BD (el evento llega tras el commit, con las colocaciones ya guardadas) y solo los ids:
                    // este hilo no tiene sesión abierta
                    Set<Long> degreeIds = degreeSubjectDataService.getDegreeIdsForSubject(subject.getId());
                    Audience audience = degreeIds.isEmpty() ? Audience.all() : Audience.degreeMembersOrUnassigned(degreeIds);
                    dispatcher.dispatch(new NotificationRequest(NotificationType.SUBJECT_CREATED, subject.getId(),
                            subject.getId(), Map.of("subjectName", subject.getName()), audience,
                            event.actorExternalId(), Set.of()));
                },
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
