package mathtexpedia.es.api.service.quiz;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.quiz.*;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;
import java.util.Optional;

public interface QuizService {

    // En todos los métodos de lectura, includeHidden solo debe ser true para administradores:
    // los cuestionarios ocultos no existen para el resto de usuarios.

    List<QuizDto> getQuizzes(boolean includeHidden);

    /** Los cuestionarios creados o editados más recientemente. */
    List<QuizDto> getRecentQuizzes(Integer limit, boolean includeHidden);

    /** Los cuestionarios más recientes de las asignaturas a las que está suscrito el usuario (vacío si no tiene suscripciones). */
    List<QuizDto> getRecentQuizzesForSubscriber(UserProfile user, Integer limit, boolean includeHidden)
            throws MathtexpediaUnauthorizedException;

    /** Los cuestionarios más hechos en general (todos los usuarios). */
    List<QuizDto> getMostAttemptedQuizzes(Integer limit, boolean includeHidden);

    Optional<QuizDto> getQuiz(long id, boolean includeHidden);

    List<QuizDto> getQuizzesBySubjectUnit(long subjectUnitId, boolean includeHidden) throws MathtexpediaNotFoundException;

    List<QuizDto> getQuizzesBySubject(long subjectId, boolean includeHidden) throws MathtexpediaNotFoundException;

    QuizForAttemptDto getQuizForAttempt(long quizId, boolean includeHidden) throws MathtexpediaNotFoundException;

    /** Uso interno (corrección de intentos): no comprueba visibilidad, quien lo llama debe haberla comprobado ya. */
    QuizForCorrectionDto getQuizForCorrection(long quizId) throws MathtexpediaNotFoundException;

    QuizDto create(CreateQuizDto dto) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException;

    QuizDto update(long quizId, UpdateQuizDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException;

    /**
     * Publica u oculta un cuestionario. Publicar uno sin preguntas es inválido. La primera publicación notifica a los
     * usuarios; ocultarlo y volver a publicarlo después no notifica de nuevo.
     */
    QuizDto setVisibility(long quizId, boolean visible) throws MathtexpediaNotFoundException, MathtexpediaInvalidException;

    void delete(long id) throws MathtexpediaNotFoundException;

    QuizExportableDto exportQuiz(long quizId) throws MathtexpediaNotFoundException;

    QuizDto importQuiz(QuizExportableDto dto, long subjectId, Long subjectUnitId) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException;
}
