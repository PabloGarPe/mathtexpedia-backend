package mathtexpedia.es.api.persistence.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.DeliveryStatus;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.Instant;

/**
 * Entrega de una notificación a un usuario por un canal. Su id es el que ve el front. Las columnas de estado
 * ({@code status}, {@code sentAt}, {@code attempts}, {@code lastError}) son el outbox del canal de correo.
 * Los enums se guardan como texto sin CHECK, ver {@link Notification}.
 */
@Getter
@Setter
@Entity
@Table(name = "notification_delivery", indexes = {
        @Index(name = "idx_delivery_user_channel_read", columnList = "user_id, channel, read_at"),
        @Index(name = "idx_delivery_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_delivery_notification", columnList = "notification_id")
})
public class NotificationDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "channel", nullable = false, length = 32)
    private String channelName;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "status", nullable = false, length = 32)
    private String statusName;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public ChannelType getChannel() {
        return channelName == null ? null : ChannelType.valueOf(channelName);
    }

    public void setChannel(ChannelType channel) {
        this.channelName = channel == null ? null : channel.name();
    }

    public DeliveryStatus getStatus() {
        return statusName == null ? null : DeliveryStatus.valueOf(statusName);
    }

    public void setStatus(DeliveryStatus status) {
        this.statusName = status == null ? null : status.name();
    }
}
