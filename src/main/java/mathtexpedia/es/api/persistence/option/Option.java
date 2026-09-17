package mathtexpedia.es.api.persistence.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import mathtexpedia.es.api.persistence.question.Question;

@Entity
@Data
@Table(name = "option")
public class Option {

    @Schema(description = "Identificador interno de la opción", accessMode = Schema.AccessMode.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Texto de la opción")
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String text;

    @Schema(description = "Indica si la opción es correcta o no")
    private boolean isCorrect;

    @Schema(description = "Posición de la opción en la pregunta")
    private int position;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @Schema(description = "Pregunta a la que pertenece la opción")
    private Question question;
}
