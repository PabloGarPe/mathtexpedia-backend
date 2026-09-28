package mathtexpedia.es.api.service.quizAttempt;

import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptDto;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptStatsDto;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttempt;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptStats;
import org.springframework.stereotype.Component;

@Component
public class QuizAttemptMapper {

    public QuizAttemptDto toDto(QuizAttempt attempt) {
        QuizAttemptDto dto = new QuizAttemptDto();
        dto.setId(attempt.getId());
        dto.setScore(attempt.getScore());
        dto.setSubmittedAt(attempt.getSubmittedAt());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        if (attempt.getQuiz() != null) dto.setQuizId(attempt.getQuiz().getId());
        if (attempt.getUser() != null) dto.setUserId(attempt.getUser().getId());
        return dto;
    }

    public QuizAttemptStatsDto toDto(QuizAttemptStats stats) {
        long totalAttempts = stats.getTotalAttempts() != null ? stats.getTotalAttempts() : 0L;
        long completedAttempts = stats.getCompletedAttempts() != null ? stats.getCompletedAttempts() : 0L;
        long totalCorrectAnswers = stats.getTotalCorrectAnswers() != null ? stats.getTotalCorrectAnswers() : 0L;
        long totalQuestionsAnswered = stats.getTotalQuestionsAnswered() != null ? stats.getTotalQuestionsAnswered() : 0L;
        double averageScore = stats.getAverageScore() != null ? stats.getAverageScore() : 0.0;
        double bestScore = stats.getBestScore() != null ? stats.getBestScore() : 0.0;

        QuizAttemptStatsDto dto = new QuizAttemptStatsDto();
        dto.setTotalAttempts((int) totalAttempts);
        dto.setCompletionRate(totalAttempts > 0 ? (completedAttempts * 100.0) / totalAttempts : 0.0);
        dto.setAccuracyPercentage(totalQuestionsAnswered > 0 ? (totalCorrectAnswers * 100.0) / totalQuestionsAnswered : 0.0);
        dto.setAverageScore(averageScore);
        dto.setBestScore(bestScore);
        return dto;
    }
}
