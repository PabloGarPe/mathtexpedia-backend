package mathtexpedia.es.api.persistence.subjectUnit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mathtexpedia.es.api.persistence.subject.Subject;

@Entity
@Table(name = "subject_unit", uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "name"}))
@Data
public class SubjectUnit {

    @Schema(description = "Identificador interno del tema", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Asignatura a la que pertenece el tema")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Schema(description = "Nombre del tema")
    @NotBlank
    @Column(nullable = false)
    private String name;

    @Schema(description = "Posición del tema dentro del orden de la asignatura")
    @Column(nullable = false)
    private int position;
}