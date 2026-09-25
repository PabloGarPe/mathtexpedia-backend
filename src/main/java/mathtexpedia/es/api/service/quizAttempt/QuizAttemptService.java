package mathtexpedia.es.api.service.quizAttempt;

import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptResultDto;
import mathtexpedia.es.api.domain.model.quizAttempt.SubmitQuizAttemptDto;
import mathtexpedia.es.api.domain.security.UserProfile;

public interface QuizAttemptService {

    QuizAttemptResultDto submitAttempt(long quizId, SubmitQuizAttemptDto dto, UserProfile user) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException;

}
