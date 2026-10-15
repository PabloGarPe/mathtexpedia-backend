package mathtexpedia.es.api.persistence.notification;

import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.NotificationScope;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class NotificationPreferenceDao extends GenericJPADao implements NotificationPreferenceDataService {

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreference> getByUser(long userAccountId) {
        logger.trace("Getting notification preferences for user {}", userAccountId);

        return em.createQuery("SELECT p FROM NotificationPreference p WHERE p.userAccount.id = :userId",
                        NotificationPreference.class)
                .setParameter("userId", userAccountId)
                .getResultList();
    }

    @Override
    @Transactional
    public void upsert(long userAccountId, NotificationType type, ChannelType channel, boolean enabled, NotificationScope scope) {
        logger.trace("Upserting notification preference for user {}: {} {} -> {}", userAccountId, type, channel, enabled);

        em.createNativeQuery("""
                        INSERT INTO notification_preference (user_id, notification_type, channel, enabled, scope, updated_at)
                        VALUES (:userId, :type, :channel, :enabled, CAST(:scope AS varchar), :now)
                        ON CONFLICT (user_id, notification_type, channel)
                        DO UPDATE SET enabled = EXCLUDED.enabled, scope = EXCLUDED.scope, updated_at = EXCLUDED.updated_at
                        """)
                .setParameter("userId", userAccountId)
                .setParameter("type", type.name())
                .setParameter("channel", channel.name())
                .setParameter("enabled", enabled)
                .setParameter("scope", scope == null ? null : scope.name())
                .setParameter("now", Instant.now())
                .executeUpdate();
    }
}
