package mathtexpedia.es.api.persistence.quizAttempt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAttemptStats {

    private Long totalAttempts;
    private Long completedAttempts;
    private Long totalCorrectAnswers;
    private Long totalQuestionsAnswered;
    private Double averageScore;
    private Double bestScore;

}
