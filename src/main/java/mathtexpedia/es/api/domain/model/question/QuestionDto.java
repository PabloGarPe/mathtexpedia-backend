package mathtexpedia.es.api.domain.model.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionDto {

    @Schema(description = "Identificador interno de la pregunta", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Texto de la pregunta")
    private String text;

    @Schema(description = "Tipo de la pregunta (multiple choice, true/false, etc.)")
    private QuestionType type;

    @Schema(description = "Explicación de la respuesta correcta de la pregunta")
    private String explanation;

    @Schema(description = "Posición de la pregunta en el cuestionario")
    private int position;

    @Schema(description = "Identificador del cuestionario al que pertenece la pregunta")
    private Long quizId;
}
