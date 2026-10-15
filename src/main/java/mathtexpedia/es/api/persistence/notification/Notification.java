package mathtexpedia.es.api.persistence.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.domain.model.notification.NotificationType;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Contenido de una notificación. Es único aunque llegue a muchos usuarios: cada destinatario tiene una fila en
 * {@link NotificationDelivery}.
 * <p>
 * Los enums se guardan como texto en campos {@code String} con accessors tipados. No pueden mapearse como enum (ni con
 * {@code @Enumerated} ni con un converter) porque Hibernate generaría un CHECK con los valores actuales que
 * {@code ddl-auto=update} no regenera: añadir un tipo o un canal nuevo rompería los INSERT en producción.
 */
@Getter
@Setter
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_resource", columnList = "resource_type, resource_id")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "type", nullable = false, length = 64)
    private String typeName;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "resource_type", nullable = false, length = 32)
    private String resourceTypeName;

    @Column(name = "resource_id")
    private Long resourceId;

    /** Asignatura relacionada, desnormalizada para poder filtrar por ella (suscripciones, #70). */
    @Column(name = "subject_id")
    private Long subjectId;

    /** Datos con los que se genera el texto al leer (nombres guardados en el momento del evento). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public NotificationType getType() {
        return typeName == null ? null : NotificationType.valueOf(typeName);
    }

    public void setType(NotificationType type) {
        this.typeName = type == null ? null : type.name();
    }

    public ResourceType getResourceType() {
        return resourceTypeName == null ? null : ResourceType.valueOf(resourceTypeName);
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceTypeName = resourceType == null ? null : resourceType.name();
    }
}
