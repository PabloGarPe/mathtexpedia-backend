package mathtexpedia.es.api.persistence.userEvent;

import jakarta.persistence.*;
import lombok.Data;
import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.Instant;

@Data
@Entity
@Table(name = "user_event")
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;
}
