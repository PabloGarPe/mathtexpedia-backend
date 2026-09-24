package mathtexpedia.es.api.domain.model.quizAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.attemptAnswer.AttemptAnswerSubmissionDto;

import java.util.List;

@Data
@AllArgsConstructor
public class SubmitQuizAttemptDto {

    @Schema(description = "Identificador del quiz asociado al intento", example = "1")
    private Long quizId;

    @Schema(description = "Lista de respuestas del intento de quiz")
    private List<AttemptAnswerSubmissionDto> answers;

}
