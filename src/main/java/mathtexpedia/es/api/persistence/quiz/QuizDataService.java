package mathtexpedia.es.api.persistence.quiz;

import java.util.List;
import java.util.Optional;

public interface QuizDataService {

    List<Quiz> getAll();

    Optional<Quiz> getById(long id);

    Quiz create(Quiz quiz);

    Quiz update(Quiz quiz);

    void delete(Quiz quiz);
}
