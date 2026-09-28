package mathtexpedia.es.api.domain.model.attemptAnswer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttemptAnswerResultDto {

    @Schema(description = "Identificador interno de la pregunta del intento de respuesta", example = "1")
    private Long questionId;

    @Schema(description = "Identificador interno de la respuesta seleccionada en el intento de respuesta", example = "2")
    private Long selectedOptionId;

    @Schema(description = "Opción correcta de la pregunta del intento de respuesta", example = "3")
    private Long correctOptionId;

    @Schema(description = "Indica si la respuesta seleccionada es correcta o incorrecta", example = "true")
    private boolean isCorrect;

    @Schema(description = "Explicación de la respuesta correcta, si existe", example = "La respuesta correcta es la opción 3 porque...")
    private String explanation;
}
