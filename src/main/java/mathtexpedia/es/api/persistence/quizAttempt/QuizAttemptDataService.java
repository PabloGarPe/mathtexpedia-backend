package mathtexpedia.es.api.persistence.quizAttempt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizAttemptDataService {

    QuizAttempt createQuizAttempt(QuizAttempt quizAttempt);

    Page<QuizAttempt> getByUserId(long userId, Pageable pageable);

    List<QuizAttempt> getByQuizIdAndUserId(long quizId, long userId);

    QuizAttemptStats getStatsByUserId(long userId);

    QuizAttemptStats getStatsByQuizIdAndUserId(long quizId, long userId);

    long countByQuizIdAndUserId(long quizId, long userId);

    Page<LeaderboardRow> getLeaderboardForQuiz(long quizId, Pageable pageable);
}
