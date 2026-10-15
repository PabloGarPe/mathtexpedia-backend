package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.CreateAnnouncementDto;
import mathtexpedia.es.api.domain.model.notification.MarkAllReadResultDto;
import mathtexpedia.es.api.domain.model.notification.NotificationDto;
import mathtexpedia.es.api.domain.model.notification.UnreadCountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.data.domain.Page;

/**
 * Notificaciones del usuario autenticado. Ningún método crea ni toca la cuenta del usuario (no llama a
 * {@code getOrProvision}): el front consulta esto periódicamente y no debe generar una escritura en cada consulta.
 */
public interface NotificationService {

    Page<NotificationDto> getMyNotifications(UserProfile user, boolean unreadOnly, int page, int size);

    UnreadCountDto getUnreadCount(UserProfile user);

    /** Idempotente: marcar como leída una ya leída no es un error. 404 si no existe o no es del usuario. */
    void markAsRead(UserProfile user, long id) throws MathtexpediaNotFoundException;

    void markAsUnread(UserProfile user, long id) throws MathtexpediaNotFoundException;

    MarkAllReadResultDto markAllAsRead(UserProfile user);

    void delete(UserProfile user, long id) throws MathtexpediaNotFoundException;

    /** Aviso manual de un administrador a todos los usuarios o a los indicados por email. El reparto es asíncrono. */
    void createAnnouncement(UserProfile admin, CreateAnnouncementDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException;
}
