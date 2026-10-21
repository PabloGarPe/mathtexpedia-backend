package mathtexpedia.es.api.persistence.courseSubscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "course_subscription",
        uniqueConstraints = @UniqueConstraint(name = "uk_course_subscription", columnNames = {"user_id", "degree_id", "course_year", "semester"}),
        indexes = @Index(name = "idx_course_subscription_degree_year", columnList = "degree_id, course_year"))
public class CourseSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    @Column(name = "course_year", nullable = false)
    private int year;

    /** 1, 2 o nulo (el año entero). */
    @Column
    private Integer semester;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
