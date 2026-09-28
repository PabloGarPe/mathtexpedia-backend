package mathtexpedia.es.api.service.subjectUnit;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.subjectUnit.CreateSubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.UpdateSubjectUnitDto;

import java.util.List;
import java.util.Optional;

public interface SubjectUnitService {

    List<SubjectUnitDto> getSubjectsUnitsBySubjectId(long subjectId) throws MathtexpediaNotFoundException;

    Optional<SubjectUnitDto> getSubjectUnit(long id);

    SubjectUnitDto create(CreateSubjectUnitDto dto, long subjectId) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    SubjectUnitDto update(long id, UpdateSubjectUnitDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;
}
