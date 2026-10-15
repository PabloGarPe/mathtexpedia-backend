package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SystemNotificationRenderer implements NotificationRenderer {

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.WELCOME, NotificationType.ANNOUNCEMENT);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            case WELCOME -> RenderedNotification.of("¡Bienvenido a Mathtexpedia!",
                    "Aquí te avisaremos de nuevos apuntes, cuestionarios y novedades.");
            case ANNOUNCEMENT -> {
                String link = text(payload, "link");
                yield new RenderedNotification(text(payload, "title"), text(payload, "message"), link.isEmpty() ? null : link);
            }
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }
}
