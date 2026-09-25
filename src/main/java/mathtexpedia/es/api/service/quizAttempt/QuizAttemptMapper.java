package mathtexpedia.es.api.service.quizAttempt;

import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptDto;
import mathtexpedia.es.api.persistence.quizAttempt.QuizAttempt;
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
}
