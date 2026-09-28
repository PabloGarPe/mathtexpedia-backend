package mathtexpedia.es.api.domain.model.quizAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizAttemptDto {

    @Schema(description = "Identificador interno del intento de quiz", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Identificador del usuario que realizó el intento de quiz", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Schema(description = "Identificador del quiz asociado al intento", example = "1")
    private Long quizId;

    @Schema(description = "Puntaje obtenido en el intento de quiz", example = "85.5")
    private double score;

    @Schema(description = "Número total de preguntas en el intento de quiz", example = "10")
    private int totalQuestions;

    @Schema(description = "Fecha y hora en que se envió el intento de quiz", accessMode = Schema.AccessMode.READ_ONLY)
    private Instant submittedAt;

}
