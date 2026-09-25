package mathtexpedia.es.api.service.quizAttempt;

import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptDto;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptResultDto;
import mathtexpedia.es.api.domain.model.quizAttempt.SubmitQuizAttemptDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizAttemptService {

    QuizAttemptResultDto submitAttempt(long quizId, SubmitQuizAttemptDto dto, UserProfile user) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException;

    Page<QuizAttemptDto> getMyAttempts(UserProfile user, Pageable pageable) throws MathtexpediaUnauthorizedException;

    List<QuizAttemptDto> getAttemptsForQuiz(long quizId, UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException;
}
