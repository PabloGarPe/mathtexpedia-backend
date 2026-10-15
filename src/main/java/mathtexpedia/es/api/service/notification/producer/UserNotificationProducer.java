package mathtexpedia.es.api.service.notification.producer;

import mathtexpedia.es.api.domain.event.NotificationListener;
import mathtexpedia.es.api.domain.event.UserProvisionedEvent;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.service.notification.Audience;
import mathtexpedia.es.api.service.notification.NotificationDispatcher;
import mathtexpedia.es.api.service.notification.NotificationRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class UserNotificationProducer {

    private final NotificationDispatcher dispatcher;

    public UserNotificationProducer(NotificationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @NotificationListener
    public void onUserProvisioned(UserProvisionedEvent event) {
        dispatcher.dispatch(new NotificationRequest(NotificationType.WELCOME, null, null, Map.of(),
                Audience.users(Set.of(event.userAccountId())), null, Set.of()));
    }
}
