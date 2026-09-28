package mathtexpedia.es.api.domain.model.attemptAnswer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttemptAnswerSubmissionDto {

    @Schema(description = "Identificador de la pregunta a la que corresponde la respuesta", example = "1")
    private Long questionId;

    @Schema(description = "Identificador de la opción seleccionada como respuesta", example = "2")
    private Long selectedOptionId;

}
