package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.persistence.notification.Notification;
import mathtexpedia.es.api.persistence.notification.NotificationDataService;
import mathtexpedia.es.api.service.notification.channel.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

/**
 * Único punto de entrada para enviar una notificación: guarda su contenido una vez y la reparte por todos los canales
 * registrados. Nunca propaga errores: un fallo al notificar no debe afectar a la operación de negocio que lo originó.
 */
@Service
public class NotificationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationDataService notificationDataService;
    private final List<NotificationChannel> channels;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public NotificationDispatcher(NotificationDataService notificationDataService, List<NotificationChannel> channels,
                                  ObjectMapper objectMapper, TransactionTemplate transactionTemplate) {
        this.notificationDataService = notificationDataService;
        this.channels = channels;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public void dispatch(NotificationRequest request) {
        logger.info("Dispatching notification {} for resource {}", request.type(), request.resourceId());

        try {
            // La excepción se captura fuera de la transacción: si no, un fallo interno la dejaría marcada rollback-only
            transactionTemplate.executeWithoutResult(status -> {
                // Sin ningún destinatario (p. ej. el único autor es quien sube el apunte) no se guarda nada
                if (deliverToChannels(request) == 0) {
                    status.setRollbackOnly();
                    logger.debug("Notification {} for resource {} has no recipients, discarded", request.type(), request.resourceId());
                }
            });
        } catch (RuntimeException e) {
            logger.error("Error dispatching notification {} for resource {}", request.type(), request.resourceId(), e);
        }
    }

    /** @return total de entregas creadas entre todos los canales */
    private int deliverToChannels(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setType(request.type());
        notification.setResourceType(request.type().resourceType());
        notification.setResourceId(request.resourceId());
        notification.setSubjectId(request.subjectId());
        notification.setPayload(objectMapper.writeValueAsString(request.payload()));
        notification.setCreatedAt(Instant.now());

        Notification created = notificationDataService.create(notification);

        int delivered = 0;
        for (NotificationChannel channel : channels)
            delivered += channel.deliver(created, request);
        return delivered;
    }
}
