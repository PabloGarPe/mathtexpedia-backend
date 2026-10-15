package mathtexpedia.es.api.persistence.notification;

import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;

public interface NotificationDataService {

    Notification create(Notification notification);

    /**
     * Reparte la notificación por el canal in-app con una única sentencia INSERT ... SELECT.
     * Solo recibe usuarios ACTIVE, respeta la preferencia del usuario para el tipo (si es configurable) y excluye al
     * actor y a los usuarios indicados. Si el tipo admite el alcance SUBSCRIBED, quien lo haya elegido solo la recibe
     * cuando está suscrito a la asignatura de la notificación.
     *
     * @param targetUserIds destinatarios; {@code null} significa todos los usuarios activos
     * @param subjectId asignatura de la notificación; hace falta para evaluar el alcance SUBSCRIBED y {@code onlySubscribers}
     * @param onlySubscribers si es true, solo reciben la notificación los suscritos a {@code subjectId}
     * @return número de entregas creadas
     */
    int fanOutInApp(long notificationId, NotificationType type, Collection<Long> targetUserIds,
                    Long subjectId, boolean onlySubscribers,
                    String excludedExternalId, Collection<Long> excludedUserIds);

    /** Bandeja del usuario, de más reciente a más antigua. */
    Page<NotificationDelivery> getInbox(String externalId, ChannelType channel, boolean unreadOnly, Pageable pageable);

    long countUnread(String externalId, ChannelType channel);

    /** @return true si la entrega existe y es del usuario (esté leída o no) */
    boolean existsDelivery(long deliveryId, String externalId);

    void markRead(long deliveryId, String externalId);

    void markUnread(long deliveryId, String externalId);

    /** @return número de entregas marcadas */
    int markAllRead(String externalId, ChannelType channel);

    /** @return true si se borró */
    boolean deleteDelivery(long deliveryId, String externalId);

    /** Borra las entregas y las notificaciones que apuntan a un recurso (p. ej. cuando se borra el recurso). */
    void deleteByResource(ResourceType resourceType, long resourceId);

    /** Entregas leídas creadas antes de la fecha. */
    int deleteReadCreatedBefore(Instant cutoff);

    /** Entregas, leídas o no, creadas antes de la fecha. */
    int deleteCreatedBefore(Instant cutoff);

    /** Notificaciones sin ninguna entrega creadas antes de la fecha (el margen evita pisar un reparto en curso). */
    int deleteOrphansCreatedBefore(Instant cutoff);
}
