package mathtexpedia.es.api.persistence.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mathtexpedia.es.api.domain.model.question.QuestionType;
import mathtexpedia.es.api.persistence.quiz.Quiz;

@Entity
@Table(name = "question")
@Data
public class Question {

    @Schema(description = "Identificador interno de la pregunta", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Texto de la pregunta")
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String text;

    @Schema(description = "Tipo de la pregunta (multiple choice, true/false, etc.)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Schema(description = "Posición de la pregunta en el cuestionario")
    private int position;

    @Schema(description = "Explicación de la respuesta correcta de la pregunta")
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    @Schema(description = "Cuestionario al que pertenece la pregunta")
    private Quiz quiz;
}
