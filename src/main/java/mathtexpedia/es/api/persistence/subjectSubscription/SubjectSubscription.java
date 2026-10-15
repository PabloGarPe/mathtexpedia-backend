package mathtexpedia.es.api.persistence.subjectSubscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Suscripción de un usuario a una asignatura. Al borrar la asignatura se borran sus suscripciones en la propia base de datos
 * ({@code ON DELETE CASCADE}), así que no bloquean su borrado.
 */
@Getter
@Setter
@Entity
@Table(name = "subject_subscription",
        uniqueConstraints = @UniqueConstraint(name = "uk_subject_subscription", columnNames = {"user_id", "subject_id"}),
        indexes = @Index(name = "idx_subject_subscription_subject", columnList = "subject_id"))
public class SubjectSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
