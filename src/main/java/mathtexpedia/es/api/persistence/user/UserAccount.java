package mathtexpedia.es.api.persistence.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import mathtexpedia.es.api.domain.model.userAccount.UserStatus;
import mathtexpedia.es.api.domain.security.UserRole;
import mathtexpedia.es.api.persistence.degree.Degree;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.Locale;

@Entity
@Data
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private String externalId;

    private String email;

    /** Nombre público que se muestra en la aplicación. Nulo hasta que el usuario completa el perfil. */
    @Column(length = 30)
    private String username;

    /** {@code username} en minúsculas: soporta la unicidad sin distinguir mayúsculas. Lo mantiene {@link #setUsername}. */
    @Column(name = "username_normalized", length = 30, unique = true)
    @Setter(AccessLevel.NONE)
    private String usernameNormalized;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 280)
    private String bio;

    @Column(length = 150)
    private String university;

    @Column(length = 150)
    private String studies;

    /** Grado del catálogo que cursa (opcional). Convive con el texto libre {@code studies}, para quien no lo encuentre en el catálogo. */
    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "degree_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Degree degree;

    /** Curso del grado en el que está (1..años del grado). Solo tiene valor si hay {@link #degree}. */
    @Column(name = "current_year")
    private Integer currentYear;

    /** Última vez que se cambió la foto de perfil; nulo si no tiene. Sirve también como versión para la caché. */
    @Column(name = "avatar_updated_at")
    private Instant avatarUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "profile_completed_at")
    private Instant profileCompletedAt;

    public void setUsername(String username) {
        this.username = username;
        this.usernameNormalized = username == null ? null : username.toLowerCase(Locale.ROOT);
    }

    public boolean hasAvatar() {
        return avatarUpdatedAt != null;
    }
}
