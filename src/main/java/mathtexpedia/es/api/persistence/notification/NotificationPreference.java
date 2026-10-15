package mathtexpedia.es.api.persistence.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.notification.ChannelType;
import mathtexpedia.es.api.domain.model.notification.NotificationScope;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.Instant;

/**
 * Preferencia de un usuario para un tipo de notificación en un canal. Es dispersa: solo existe lo que el usuario ha
 * cambiado; si no hay fila se usan los valores por defecto de {@link NotificationType}.
 * Los enums se guardan como texto sin CHECK, ver {@link Notification}.
 */
@Getter
@Setter
@Entity
@Table(name = "notification_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uk_notification_preference", columnNames = {"user_id", "notification_type", "channel"})
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "notification_type", nullable = false, length = 64)
    private String typeName;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "channel", nullable = false, length = 32)
    private String channelName;

    @Column(nullable = false)
    private boolean enabled;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "scope", length = 32)
    private String scopeName;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public NotificationType getType() {
        return typeName == null ? null : NotificationType.valueOf(typeName);
    }

    public void setType(NotificationType type) {
        this.typeName = type == null ? null : type.name();
    }

    public ChannelType getChannel() {
        return channelName == null ? null : ChannelType.valueOf(channelName);
    }

    public void setChannel(ChannelType channel) {
        this.channelName = channel == null ? null : channel.name();
    }

    public NotificationScope getScope() {
        return scopeName == null ? null : NotificationScope.valueOf(scopeName);
    }

    public void setScope(NotificationScope scope) {
        this.scopeName = scope == null ? null : scope.name();
    }
}
