package mathtexpedia.es.api.domain.model.quizAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.attemptAnswer.AttemptAnswerResultDto;

import java.util.List;

@Data
@AllArgsConstructor
public class QuizAttemptResultDto {

    @Schema(description = "Identificador interno del intento de cuestionario", example = "1")
    private Long id;

    @Schema(description = "Puntuación obtenida en el intento de cuestionario", example = "85.5")
    private double score;

    @Schema(description = "Número total de preguntas en el intento de cuestionario", example = "10")
    private int totalQuestions;

    @Schema(description = "Porcentaje de respuestas correctas en el intento de cuestionario", example = "85.0")
    private double percentage;

    @Schema(description = "Lista de respuestas del intento de cuestionario")
    private List<AttemptAnswerResultDto> answers;
}
