package mathtexpedia.es.api.persistence.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import mathtexpedia.es.api.domain.model.quiz.Difficulty;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;

import java.util.Date;

@Entity
@Table(name = "quiz")
@Data
public class Quiz {

    @Schema(description = "Identificador interno del cuestionario", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Nombre del cuestionario")
    @Column(nullable = false)
    @NotBlank
    private String name;

    @Schema(description = "Descripción opcional del cuestionario")
    @Column(length = 500)
    private String description;

    @Schema(description = "Dificultad del cuestionario")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Schema(description = "Fecha de última edición del cuestionario", accessMode = Schema.AccessMode.READ_ONLY)
    @Column(name = "last_time_edited")
    private Date lastTimeEdited;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @Schema(description = "Asignatura a la que pertenece el cuestionario")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "subject_unit_id")
    @Schema(description = "Tema al que pertenece el cuestionario")
    private SubjectUnit subjectUnit;
}
