package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class QuizNotificationRenderer implements NotificationRenderer {

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.QUIZ_PUBLISHED);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        return RenderedNotification.of("Nuevo cuestionario en " + text(payload, "subjectName"),
                "Ya puedes hacer «" + text(payload, "quizName") + "».");
    }
}
