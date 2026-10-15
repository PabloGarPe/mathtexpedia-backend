package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.persistence.notification.NotificationDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Cuando se borra un recurso, sus notificaciones dejan de tener sentido y se borran. */
@Component
public class NotificationResourceCleanupListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationResourceCleanupListener.class);

    private final NotificationDataService notificationDataService;

    public NotificationResourceCleanupListener(NotificationDataService notificationDataService) {
        this.notificationDataService = notificationDataService;
    }

    @NotificationListener
    public void onResourceDeleted(ResourceDeletedEvent event) {
        try {
            notificationDataService.deleteByResource(event.resourceType(), event.resourceId());
        } catch (RuntimeException e) {
            logger.error("Error deleting notifications of {} {}", event.resourceType(), event.resourceId(), e);
        }
    }
}
