package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.model.notification.NotificationType;

import java.util.Map;
import java.util.Set;

/**
 * Lo que un producer pide enviar. El recurso al que apunta se deduce del tipo
 * ({@link NotificationType#resourceType()}); {@code resourceId} es su id.
 *
 * @param payload           datos con los que el renderer genera el texto (nombres guardados en este momento)
 * @param actorExternalId   quien provoca la notificación; no la recibe aunque esté en la audiencia. Puede ser nulo
 * @param excludedUserIds   usuarios que no deben recibirla (p. ej. los que ya reciben otra más específica)
 */
public record NotificationRequest(NotificationType type, Long resourceId, Long subjectId, Map<String, Object> payload,
                                  Audience audience, String actorExternalId, Set<Long> excludedUserIds) {

    public NotificationRequest {
        payload = payload == null ? Map.of() : payload;
        excludedUserIds = excludedUserIds == null ? Set.of() : excludedUserIds;
    }
}
