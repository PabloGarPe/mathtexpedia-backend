package mathtexpedia.es.api.persistence.subjectUnit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mathtexpedia.es.api.persistence.subject.Subject;

@Entity
@Table(name = "subject_unit", uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "name"}))
@Data
public class SubjectUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int position;
}