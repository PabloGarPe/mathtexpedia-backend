package mathtexpedia.es.api.service.subject;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDetailDto;
import mathtexpedia.es.api.domain.model.subject.SubjectFilter;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;

import java.util.List;
import java.util.Optional;

public interface SubjectService {

    /** Asignaturas con sus colocaciones, filtradas opcionalmente por grado, año, cuatrimestre o sin clasificar. */
    List<SubjectDetailDto> getSubjects(SubjectFilter filter) throws MathtexpediaNotFoundException, MathtexpediaInvalidException;

    Optional<SubjectDetailDto> getSubject(long id);

    SubjectDetailDto create(CreateSubjectDto dto) throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException;

    void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    SubjectDetailDto update(long id, UpdateSubjectDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException;
}
