package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.persistence.notification.NotificationDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Retención de notificaciones: las leídas caducan antes que las no leídas. */
@Component
public class NotificationCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(NotificationCleanupJob.class);

    /** Margen para no borrar una notificación cuyo reparto aún está en curso. */
    private static final Duration ORPHAN_GRACE = Duration.ofDays(1);

    private final NotificationDataService notificationDataService;
    private final long readRetentionDays;
    private final long maxRetentionDays;

    public NotificationCleanupJob(NotificationDataService notificationDataService,
                                  @Value("${notifications.retention.read-days}") long readRetentionDays,
                                  @Value("${notifications.retention.max-days}") long maxRetentionDays) {
        this.notificationDataService = notificationDataService;
        this.readRetentionDays = readRetentionDays;
        this.maxRetentionDays = maxRetentionDays;
    }

    @Scheduled(cron = "${notifications.cleanup.cron}")
    public void cleanup() {
        Instant now = Instant.now();

        try {
            int read = notificationDataService.deleteReadCreatedBefore(now.minus(Duration.ofDays(readRetentionDays)));
            int old = notificationDataService.deleteCreatedBefore(now.minus(Duration.ofDays(maxRetentionDays)));
            int orphans = notificationDataService.deleteOrphansCreatedBefore(now.minus(ORPHAN_GRACE));

            logger.info("Notification cleanup: {} read deliveries, {} old deliveries, {} orphan notifications deleted",
                    read, old, orphans);
        } catch (RuntimeException e) {
            logger.error("Error cleaning up notifications", e);
        }
    }
}
