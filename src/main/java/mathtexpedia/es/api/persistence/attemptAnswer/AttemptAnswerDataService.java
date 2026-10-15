package mathtexpedia.es.api.persistence.attemptAnswer;


import java.util.List;

public interface AttemptAnswerDataService {

    List<AttemptAnswer> getAttemptAnswersByAttempt(long attemptId);

    AttemptAnswer saveAttempt(AttemptAnswer attempt);

    /** Borra las respuestas de todos los intentos de un cuestionario. */
    void deleteByQuizId(long quizId);
}
