package mathtexpedia.es.api.persistence.notification;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.DeliveryStatus;
import mathtexpedia.es.api.domain.model.notification.NotificationScope;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

@Repository
public class NotificationDao extends GenericJPADao implements NotificationDataService {

    private static final String EXTERNAL_ID_PARAM = "externalId";
    private static final String CHANNEL_PARAM = "channel";

    @Override
    @Transactional
    public Notification create(Notification notification) {
        logger.trace("Creating notification of type {}", notification.getType());

        em.persist(notification);
        return notification;
    }

    @Override
    @Transactional
    public int fanOutInApp(long notificationId, NotificationType type, Collection<Long> targetUserIds,
                           Collection<Long> degreeIds, Long subjectId, boolean onlySubscribers,
                           String excludedExternalId, Collection<Long> excludedUserIds) {
        logger.trace("Fanning out notification {} of type {}", notificationId, type);

        boolean hasTargets = targetUserIds != null;
        if (hasTargets && targetUserIds.isEmpty())
            return 0;
        if (onlySubscribers && subjectId == null)
            return 0;

        boolean checkPreference = type.configurable();
        boolean hasActor = excludedExternalId != null;
        boolean hasDegrees = degreeIds != null && !degreeIds.isEmpty();
        boolean hasExcluded = excludedUserIds != null && !excludedUserIds.isEmpty();
        boolean checkScope = checkPreference && type.supportedScopes().contains(NotificationScope.SUBSCRIBED);

        // El SQL se construye según haga falta en vez de pasar parámetros nulos (Postgres no infiere su tipo en "? IS NULL").
        StringBuilder sql = new StringBuilder("""
                INSERT INTO notification_delivery (notification_id, user_id, channel, status, created_at, attempts)
                SELECT :notificationId, u.id, :channel, :status, :now, 0
                FROM user_account u
                """);
        if (checkPreference)
            sql.append("""
                    LEFT JOIN notification_preference p
                           ON p.user_id = u.id AND p.notification_type = :type AND p.channel = :channel
                    """);
        sql.append("WHERE u.status = 'ACTIVE'\n");
        if (checkPreference)
            sql.append("  AND COALESCE(p.enabled, :defaultEnabled) = true\n");
        if (checkScope) {
            // Quien eligió SUBSCRIBED solo la recibe si está suscrito a la asignatura (si no hay asignatura, nunca)
            sql.append("  AND (COALESCE(p.scope, :defaultScope) <> 'SUBSCRIBED'");
            if (subjectId != null)
                sql.append(" OR EXISTS (SELECT 1 FROM subject_subscription ss WHERE ss.user_id = u.id AND ss.subject_id = :subjectId)");
            sql.append(")\n");
        }
        if (onlySubscribers)
            sql.append("  AND EXISTS (SELECT 1 FROM subject_subscription sa WHERE sa.user_id = u.id AND sa.subject_id = :subjectId)\n");
        if (hasActor)
            sql.append("  AND u.external_id <> :actor\n");
        if (hasExcluded)
            sql.append("  AND u.id NOT IN (:excluded)\n");
        if (hasTargets)
            sql.append("  AND u.id IN (:targets)\n");
        if (hasDegrees)
            sql.append("  AND (u.degree_id IS NULL OR u.degree_id IN (:degreeIds))\n");

        Query query = em.createNativeQuery(sql.toString())
                .setParameter("notificationId", notificationId)
                .setParameter(CHANNEL_PARAM, ChannelType.IN_APP.name())
                .setParameter("status", DeliveryStatus.DELIVERED.name())
                .setParameter("now", Instant.now());
        if (checkPreference)
            query.setParameter("type", type.name())
                    .setParameter("defaultEnabled", type.defaultEnabled(ChannelType.IN_APP));
        if (checkScope)
            query.setParameter("defaultScope", type.defaultScope().name());
        if (subjectId != null && (onlySubscribers || checkScope))
            query.setParameter("subjectId", subjectId);
        if (hasActor)
            query.setParameter("actor", excludedExternalId);
        if (hasExcluded)
            query.setParameter("excluded", excludedUserIds);
        if (hasTargets)
            query.setParameter("targets", targetUserIds);
        if (hasDegrees)
            query.setParameter("degreeIds", degreeIds);

        return query.executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDelivery> getInbox(String externalId, ChannelType channel, boolean unreadOnly, Pageable pageable) {
        logger.trace("Getting notification inbox for user {} (unreadOnly: {})", externalId, unreadOnly);

        String where = "WHERE d.userAccount.externalId = :externalId AND d.channelName = :channel"
                + (unreadOnly ? " AND d.readAt IS NULL" : "");

        TypedQuery<NotificationDelivery> query = em.createQuery(
                "SELECT d FROM NotificationDelivery d JOIN FETCH d.notification " + where
                        + " ORDER BY d.createdAt DESC, d.id DESC",
                NotificationDelivery.class);
        query.setParameter(EXTERNAL_ID_PARAM, externalId);
        query.setParameter(CHANNEL_PARAM, channel.name());
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = em.createQuery("SELECT COUNT(d) FROM NotificationDelivery d " + where, Long.class)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .setParameter(CHANNEL_PARAM, channel.name())
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String externalId, ChannelType channel) {
        logger.trace("Counting unread notifications for user {}", externalId);

        return em.createQuery("""
                        SELECT COUNT(d) FROM NotificationDelivery d
                        WHERE d.userAccount.externalId = :externalId AND d.channelName = :channel AND d.readAt IS NULL
                        """, Long.class)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .setParameter(CHANNEL_PARAM, channel.name())
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsDelivery(long deliveryId, String externalId) {
        logger.trace("Checking delivery {} belongs to user {}", deliveryId, externalId);

        Number count = (Number) em.createNativeQuery("""
                        SELECT COUNT(*) FROM notification_delivery d
                        JOIN user_account u ON u.id = d.user_id
                        WHERE d.id = :id AND u.external_id = :externalId
                        """)
                .setParameter("id", deliveryId)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    @Transactional
    public void markRead(long deliveryId, String externalId) {
        logger.trace("Marking delivery {} as read for user {}", deliveryId, externalId);

        em.createNativeQuery("""
                        UPDATE notification_delivery
                        SET read_at = :now
                        WHERE id = :id AND read_at IS NULL
                          AND user_id = (SELECT id FROM user_account WHERE external_id = :externalId)
                        """)
                .setParameter("now", Instant.now())
                .setParameter("id", deliveryId)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void markUnread(long deliveryId, String externalId) {
        logger.trace("Marking delivery {} as unread for user {}", deliveryId, externalId);

        em.createNativeQuery("""
                        UPDATE notification_delivery
                        SET read_at = NULL
                        WHERE id = :id AND read_at IS NOT NULL
                          AND user_id = (SELECT id FROM user_account WHERE external_id = :externalId)
                        """)
                .setParameter("id", deliveryId)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int markAllRead(String externalId, ChannelType channel) {
        logger.trace("Marking all notifications as read for user {}", externalId);

        return em.createNativeQuery("""
                        UPDATE notification_delivery
                        SET read_at = :now
                        WHERE read_at IS NULL AND channel = :channel
                          AND user_id = (SELECT id FROM user_account WHERE external_id = :externalId)
                        """)
                .setParameter("now", Instant.now())
                .setParameter(CHANNEL_PARAM, channel.name())
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public boolean deleteDelivery(long deliveryId, String externalId) {
        logger.trace("Deleting delivery {} for user {}", deliveryId, externalId);

        return em.createNativeQuery("""
                        DELETE FROM notification_delivery
                        WHERE id = :id
                          AND user_id = (SELECT id FROM user_account WHERE external_id = :externalId)
                        """)
                .setParameter("id", deliveryId)
                .setParameter(EXTERNAL_ID_PARAM, externalId)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public void deleteByResource(ResourceType resourceType, long resourceId) {
        logger.trace("Deleting notifications of resource {} {}", resourceType, resourceId);

        em.createNativeQuery("""
                        DELETE FROM notification_delivery
                        WHERE notification_id IN (
                            SELECT id FROM notification WHERE resource_type = :resourceType AND resource_id = :resourceId)
                        """)
                .setParameter("resourceType", resourceType.name())
                .setParameter("resourceId", resourceId)
                .executeUpdate();

        em.createNativeQuery("DELETE FROM notification WHERE resource_type = :resourceType AND resource_id = :resourceId")
                .setParameter("resourceType", resourceType.name())
                .setParameter("resourceId", resourceId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteReadCreatedBefore(Instant cutoff) {
        logger.trace("Deleting read deliveries created before {}", cutoff);

        return em.createNativeQuery("DELETE FROM notification_delivery WHERE read_at IS NOT NULL AND created_at < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteCreatedBefore(Instant cutoff) {
        logger.trace("Deleting deliveries created before {}", cutoff);

        return em.createNativeQuery("DELETE FROM notification_delivery WHERE created_at < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteOrphansCreatedBefore(Instant cutoff) {
        logger.trace("Deleting orphan notifications created before {}", cutoff);

        return em.createNativeQuery("""
                        DELETE FROM notification n
                        WHERE n.created_at < :cutoff
                          AND NOT EXISTS (SELECT 1 FROM notification_delivery d WHERE d.notification_id = n.id)
                        """)
                .setParameter("cutoff", cutoff)
                .executeUpdate();
    }
}
