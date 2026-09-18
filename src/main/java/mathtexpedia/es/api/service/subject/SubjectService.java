package mathtexpedia.es.api.service.subject;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;

import java.util.List;
import java.util.Optional;

public interface SubjectService {

    List<SubjectDto> getSubjects();

    Optional<SubjectDto> getSubject(long id);

    SubjectDto create(CreateSubjectDto dto) throws MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    SubjectDto update(long id, UpdateSubjectDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;
}
