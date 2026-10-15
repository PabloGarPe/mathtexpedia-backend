package mathtexpedia.es.api.persistence.quiz;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface QuizDataService {

    /** @param visibleOnly si es true, excluye los cuestionarios ocultos */
    List<Quiz> getAll(boolean visibleOnly);

    Optional<Quiz> getById(long id);

    List<Quiz> getAllBySubjectId(long subjectId, boolean visibleOnly);

    List<Quiz> getAllBySubjectUnitId(long subjectUnitId, boolean visibleOnly);

    /** Los {@code limit} cuestionarios creados o editados más recientemente, del más reciente al más antiguo. */
    List<Quiz> getRecent(int limit, boolean visibleOnly);

    List<Quiz> getByIds(Collection<Long> quizIds);

    Quiz create(Quiz quiz);

    Quiz update(Quiz quiz);

    void delete(Quiz quiz);
}
