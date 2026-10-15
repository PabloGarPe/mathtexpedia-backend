package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Localiza el renderer de cada tipo. Falla al arrancar si algún tipo no tiene renderer (o tiene más de uno), para que
 * nadie añada un tipo nuevo olvidando su texto.
 */
@Component
public class NotificationRendererRegistry {

    private final Map<NotificationType, NotificationRenderer> renderers = new EnumMap<>(NotificationType.class);

    public NotificationRendererRegistry(List<NotificationRenderer> rendererList) {
        for (NotificationRenderer renderer : rendererList) {
            for (NotificationType type : renderer.supportedTypes()) {
                if (renderers.put(type, renderer) != null)
                    throw new IllegalStateException("More than one NotificationRenderer for type " + type);
            }
        }

        for (NotificationType type : NotificationType.values()) {
            if (!renderers.containsKey(type))
                throw new IllegalStateException("No NotificationRenderer registered for type " + type);
        }
    }

    public RenderedNotification render(NotificationType type, Map<String, Object> payload) {
        return renderers.get(type).render(type, payload);
    }
}
