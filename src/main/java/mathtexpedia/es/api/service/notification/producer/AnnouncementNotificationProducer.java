package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.AnnouncementCreatedEvent;
import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class AnnouncementNotificationProducer {

    private final NotificationDispatcher dispatcher;

    public AnnouncementNotificationProducer(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onAnnouncementCreated(AnnouncementCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", event.title());
        payload.put("message", event.message());
        if (event.link() != null && !event.link().isBlank())
            payload.put("link", event.link());

        boolean everyone = event.recipientUserIds() == null || event.recipientUserIds().isEmpty();
        Audience audience = everyone ? Audience.all() : Audience.users(Set.copyOf(event.recipientUserIds()));

        // Sin actor excluido: el administrador que lo envía también lo ve, como confirmación
        dispatcher.dispatch(new NotificationRequest(NotificationType.ANNOUNCEMENT, null, null, payload, audience, null, Set.of()));
    }
}
