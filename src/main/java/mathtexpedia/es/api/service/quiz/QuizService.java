package mathtexpedia.es.api.service.quiz;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.quiz.*;

import java.util.List;
import java.util.Optional;

public interface QuizService {

    List<QuizDto> getQuizzes();

    Optional<QuizDto> getQuiz(long id);

    List<QuizDto> getQuizzesBySubjectUnit(long subjectUnitId) throws MathtexpediaNotFoundException;

    List<QuizDto> getQuizzesBySubject(long subjectId) throws MathtexpediaNotFoundException;

    QuizForAttemptDto getQuizForAttempt(long quizId) throws MathtexpediaNotFoundException;

    QuizForCorrectionDto getQuizForCorrection(long quizId) throws MathtexpediaNotFoundException;

    QuizDto create(CreateQuizDto dto) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException;

    QuizDto update(long quizId, UpdateQuizDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException;

    QuizExportableDto exportQuiz(long quizId) throws MathtexpediaNotFoundException;

    QuizDto importQuiz(QuizExportableDto dto, long subjectId, Long subjectUnitId) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException;
}
