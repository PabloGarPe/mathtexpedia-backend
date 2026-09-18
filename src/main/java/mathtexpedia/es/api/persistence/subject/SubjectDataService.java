package mathtexpedia.es.api.persistence.subject;

import java.util.List;
import java.util.Optional;

public interface SubjectDataService {

    List<Subject> getAll();

    Optional<Subject> getById(long id);

    Subject create(Subject subject);

    Subject update(Subject subject);

    void delete(Subject subject);
}
