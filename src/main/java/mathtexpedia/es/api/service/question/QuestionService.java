package mathtexpedia.es.api.service.question;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.question.CreateQuestionDto;
import mathtexpedia.es.api.domain.model.question.QuestionDto;
import mathtexpedia.es.api.domain.model.question.UpdateQuestionDto;

import java.util.List;
import java.util.Optional;

public interface QuestionService {

    List<QuestionDto> getQuestionsByQuiz(long quizId) throws MathtexpediaNotFoundException;

    Optional<QuestionDto> getQuestion(long id);

    QuestionDto create(CreateQuestionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    QuestionDto update(long questionId, UpdateQuestionDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException;
}
