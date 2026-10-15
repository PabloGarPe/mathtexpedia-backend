package mathtexpedia.es.api.persistence.quiz;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import mathtexpedia.es.api.domain.model.quiz.Difficulty;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;

import java.time.Instant;

@Entity
@Table(name = "quiz")
@Data
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(name = "last_time_edited")
    private Instant lastTimeEdited;

    /**
     * Los cuestionarios nuevos nacen ocultos. El default de la columna hace que las filas existentes queden visibles
     * al añadirla con {@code ddl-auto=update}.
     */
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean visible;

    /** Primera vez que se hizo visible. Solo esa primera publicación notifica a los usuarios. */
    @Column(name = "published_at")
    private Instant publishedAt;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "subject_unit_id")
    private SubjectUnit subjectUnit;
}
