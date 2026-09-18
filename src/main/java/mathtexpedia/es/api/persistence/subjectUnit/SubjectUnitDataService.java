package mathtexpedia.es.api.persistence.subjectUnit;

import java.util.List;
import java.util.Optional;

public interface SubjectUnitDataService {

    List<SubjectUnit> getAllForSubject(long subjectId);

    Optional<SubjectUnit> getById(long id);

    SubjectUnit create(SubjectUnit subjectUnit);

    SubjectUnit update(SubjectUnit subjectUnit);

    void delete(SubjectUnit subjectUnit);
}
