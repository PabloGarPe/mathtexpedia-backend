package mathtexpedia.es.api.service.degree;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.degree.CreateDegreeDto;
import mathtexpedia.es.api.domain.model.degree.CurriculumDto;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;
import mathtexpedia.es.api.domain.model.degree.UpdateDegreeDto;

import java.util.List;
import java.util.Optional;

public interface DegreeService {

    List<DegreeDto> getDegrees();

    Optional<DegreeDto> getDegree(long id);

    /** Plan de estudios del grado: sus asignaturas agrupadas por año, filtradas opcionalmente por año y cuatrimestre. */
    CurriculumDto getCurriculum(long id, Integer year, Integer semester) throws MathtexpediaNotFoundException, MathtexpediaInvalidException;

    DegreeDto create(CreateDegreeDto dto) throws MathtexpediaConflictException;

    DegreeDto update(long id, UpdateDegreeDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException;
}
