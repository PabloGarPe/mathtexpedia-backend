package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SubjectNotificationRenderer implements NotificationRenderer {

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.SUBJECT_CREATED, NotificationType.SUBJECT_UNIT_CREATED);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        if (type == NotificationType.SUBJECT_UNIT_CREATED)
            return RenderedNotification.of("Nuevo tema",
                    "Se ha añadido el tema " + text(payload, "subjectUnitName") + " a " + text(payload, "subjectName") + ".");

        return RenderedNotification.of("Nueva asignatura",
                "Se ha añadido la asignatura " + text(payload, "subjectName") + ".");
    }
}
