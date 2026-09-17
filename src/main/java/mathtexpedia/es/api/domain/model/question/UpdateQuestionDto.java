package mathtexpedia.es.api.domain.model.question;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuestionDto {

    @Schema(description = "Texto de la pregunta",
            example = "¿Cuál es la capital de Francia?")
    @NotNull
    @NotBlank
    private String text;

    @Schema(description = "Tipo de la pregunta (multiple choice, true/false, etc.)",
            example = "MULTIPLE_CHOICE")
    @NotNull
    private QuestionType type;

    @Schema(description = "Posición de la pregunta en el cuestionario",
            example = "1")
    private int position;

    @Schema(description = "Explicación de la respuesta correcta de la pregunta",
            example = "La capital de Francia es París.",
            nullable = true)
    private String explanation;

    @Schema(description = "Identificador del cuestionario al que pertenece la pregunta",
            example = "1")
    @NotNull
    private Long quizId;

}

