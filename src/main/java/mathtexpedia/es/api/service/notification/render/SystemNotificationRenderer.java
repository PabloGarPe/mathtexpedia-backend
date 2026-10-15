package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SystemNotificationRenderer implements NotificationRenderer {

    /** Ruta del front donde se completa el perfil. */
    private static final String PROFILE_LINK = "/dashboard/profile";

    @Override
    public Set<NotificationType> supportedTypes() {
        return Set.of(NotificationType.WELCOME, NotificationType.ANNOUNCEMENT);
    }

    @Override
    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        return switch (type) {
            // Solo se envía al crearse la cuenta, cuando el perfil siempre está por completar
            case WELCOME -> new RenderedNotification("¡Bienvenido a Mathtexpedia!",
                    "Completa tu perfil para elegir tu nombre de usuario. Aquí te avisaremos de nuevos apuntes, cuestionarios y novedades.",
                    PROFILE_LINK);
            case ANNOUNCEMENT -> {
                String link = text(payload, "link");
                yield new RenderedNotification(text(payload, "title"), text(payload, "message"), link.isEmpty() ? null : link);
            }
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }
}
