package mathtexpedia.es.api.service.notification.render;

import mathtexpedia.es.api.domain.model.notification.NotificationType;

import java.util.Map;
import java.util.Set;

/**
 * Genera el texto de un tipo de notificación a partir de su payload. Se ejecuta al leer, no al guardar: cambiar un
 * texto no obliga a migrar los datos, y el canal de correo reutilizará los mismos renderers.
 */
public interface NotificationRenderer {

    Set<NotificationType> supportedTypes();

    RenderedNotification render(NotificationType type, Map<String, Object> payload);

    /** Valor de texto del payload o cadena vacía si no está. */
    default String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : value.toString();
    }
}
