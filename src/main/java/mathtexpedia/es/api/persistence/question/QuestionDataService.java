package mathtexpedia.es.api.persistence.question;

import java.util.List;
import java.util.Optional;

public interface QuestionDataService {

    List<Question> getAllQuestionsByQuizId(Long quizId);

    Optional<Question> getQuestionById(Long id);

    Question create(Question question);

    Question update(Question question);

    void delete(Question question);
}
