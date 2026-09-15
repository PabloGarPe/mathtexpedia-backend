package mathtexpedia.es.api.service.quiz;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.quiz.CreateQuizDto;
import mathtexpedia.es.api.domain.model.quiz.QuizDto;
import mathtexpedia.es.api.domain.model.quiz.UpdateQuizDto;

import java.util.List;
import java.util.Optional;

public interface QuizService {

    List<QuizDto> getQuizzes();

    Optional<QuizDto> getQuiz(long id);

    List<QuizDto> getQuizzesBySubjectUnit(long subjectUnitId);

    List<QuizDto> getQuizzesBySubject(long subjectId);

    QuizDto create(CreateQuizDto dto) throws MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException;

    QuizDto update(long quizId, UpdateQuizDto dto) throws MathtexpediaNotFoundException, MathtexpediaInvalidException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException;
}
