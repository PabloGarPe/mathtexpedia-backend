package mathtexpedia.es.api.persistence.quizAttempt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface QuizAttemptDataService {

    QuizAttempt createQuizAttempt(QuizAttempt quizAttempt);

    Page<QuizAttempt> getByUserId(long userId, Pageable pageable);

    List<QuizAttempt> getByQuizIdAndUserId(long quizId, long userId);

    QuizAttemptStats getStatsByUserId(long userId);

    QuizAttemptStats getStatsByQuizIdAndUserId(long quizId, long userId);

    long countByQuizIdAndUserId(long quizId, long userId);

    /** Intentos totales (todos los usuarios) de cada cuestionario indicado. Los que nadie ha hecho no aparecen en el mapa. */
    Map<Long, Long> countAttemptsByQuizIds(Collection<Long> quizIds);

    /** Ids de los cuestionarios más hechos en general (al menos un intento), de más a menos. */
    List<Long> getMostAttemptedQuizIds(int limit);

    /** Borra todos los intentos de un cuestionario. Antes hay que borrar sus respuestas ({@code AttemptAnswerDataService.deleteByQuizId}). */
    void deleteByQuizId(long quizId);

    Page<LeaderboardRow> getLeaderboardForQuiz(long quizId, Pageable pageable);
}
