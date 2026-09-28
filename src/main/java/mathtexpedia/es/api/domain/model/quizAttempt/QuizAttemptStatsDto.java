package mathtexpedia.es.api.domain.model.quizAttempt;

import lombok.Data;

@Data
public class QuizAttemptStatsDto {

    private int totalAttempts;
    private double completionRate;
    private double accuracyPercentage;
    private double averageScore;
    private double bestScore;

}
