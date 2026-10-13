package mathtexpedia.es.api.domain.model.quizAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class QuizAttemptStatsDto {

    @Schema(description = "Número de intentos del usuario autenticado; 0 significa que aún no ha intentado el cuestionario", example = "3")
    private int totalAttempts;

    @Schema(description = "Porcentaje de intentos sin preguntas sin responder", example = "100.0")
    private double completionRate;

    @Schema(description = "Porcentaje de aciertos sobre las preguntas respondidas", example = "75.0")
    private double accuracyPercentage;

    @Schema(description = "Puntuación media sobre 10", example = "6.5")
    private double averageScore;

    @Schema(description = "Mejor puntuación sobre 10 (0 si no hay intentos)", example = "8.0")
    private double bestScore;

}
