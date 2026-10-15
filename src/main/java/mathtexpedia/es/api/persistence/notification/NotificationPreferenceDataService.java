package mathtexpedia.es.api.persistence.notification;

import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.NotificationScope;
import mathtexpedia.es.api.domain.model.notification.NotificationType;

import java.util.List;

public interface NotificationPreferenceDataService {

    List<NotificationPreference> getByUser(long userAccountId);

    /** Crea o actualiza la preferencia (única por usuario, tipo y canal). {@code scope} puede ser nulo. */
    void upsert(long userAccountId, NotificationType type, ChannelType channel, boolean enabled, NotificationScope scope);
}
