package mathtexpedia.es.api.domain.model.attemptAnswer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttemptAnswerDto {

    @Schema(description = "Identificador interno de la respuesta del intento", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Identificador del intento de quiz asociado a la respuesta", example = "1")
    private Long attemptId;

    @Schema(description = "Identificador de la pregunta asociada a la respuesta", example = "1")
    private Long questionId;

    @Schema(description = "Identificador de la opción seleccionada en la respuesta", example = "1")
    private Long selectedOptionId;

    @Schema(description = "Indica si la respuesta es correcta o incorrecta", example = "true")
    private boolean isCorrect;
}
