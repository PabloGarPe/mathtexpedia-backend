package mathtexpedia.es.api.service.notification.channel;

import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.persistence.notification.Notification;
import mathtexpedia.es.api.service.notification.NotificationRequest;

/**
 * Un canal de entrega. Spring inyecta todos los beans en el dispatcher, así que añadir uno (p. ej. el correo) es
 * implementar esta interfaz; no hay que tocar el dispatcher ni los producers.
 */
public interface NotificationChannel {

    ChannelType type();

    /**
     * Crea las entregas de la notificación para los destinatarios que corresponda en este canal.
     *
     * @return número de entregas creadas
     */
    int deliver(Notification notification, NotificationRequest request);
}
