package mathtexpedia.es.api.service.notification.channel;

import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.persistence.notification.Notification;
import mathtexpedia.es.api.persistence.notification.NotificationDataService;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

/** Notificaciones dentro de la app (la campanita): una fila por destinatario, ya entregada. */
@Component
public class InAppNotificationChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(InAppNotificationChannel.class);

    private final NotificationDataService notificationDataService;

    public InAppNotificationChannel(NotificationDataService notificationDataService) {
        this.notificationDataService = notificationDataService;
    }

    @Override
    public ChannelType type() {
        return ChannelType.IN_APP;
    }

    @Override
    public int deliver(Notification notification, NotificationRequest request) {
        Collection<Long> targetUserIds = switch (request.audience()) {
            case Audience.AllUsers ignored -> null;
            case Audience.Users users -> users.userIds();
        };

        int delivered = notificationDataService.fanOutInApp(
                notification.getId(), request.type(), targetUserIds, request.actorExternalId(), request.excludedUserIds());

        logger.info("Notification {} ({}) delivered in-app to {} users", notification.getId(), request.type(), delivered);
        return delivered;
    }
}
