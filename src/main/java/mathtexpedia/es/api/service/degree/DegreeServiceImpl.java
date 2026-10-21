package mathtexpedia.es.api.service.degree;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.degree.CreateDegreeDto;
import mathtexpedia.es.api.domain.model.degree.CurriculumDto;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;
import mathtexpedia.es.api.domain.model.degree.UpdateDegreeDto;
import mathtexpedia.es.api.persistence.courseSubscription.CourseSubscriptionDataService;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.degree.DegreeDataService;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubjectDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DegreeServiceImpl implements DegreeService {

    private static final Logger logger = LoggerFactory.getLogger(DegreeServiceImpl.class);

    private final DegreeDataService degreeDataService;
    private final DegreeSubjectDataService degreeSubjectDataService;
    private final CourseSubscriptionDataService courseSubscriptionDataService;
    private final DegreeMapper degreeMapper;

    public DegreeServiceImpl(DegreeDataService degreeDataService, DegreeSubjectDataService degreeSubjectDataService,
                             CourseSubscriptionDataService courseSubscriptionDataService, DegreeMapper degreeMapper) {
        this.degreeDataService = degreeDataService;
        this.degreeSubjectDataService = degreeSubjectDataService;
        this.courseSubscriptionDataService = courseSubscriptionDataService;
        this.degreeMapper = degreeMapper;
    }

    @Override
    public List<DegreeDto> getDegrees() {
        logger.info("Fetching all degrees");

        return degreeDataService.getAll()
                .stream()
                .map(degreeMapper::toDto)
                .toList();
    }

    @Override
    public Optional<DegreeDto> getDegree(long id) {
        logger.info("Fetching degree with id: {}", id);

        return degreeDataService.getById(id).map(degreeMapper::toDto);
    }

    @Override
    public CurriculumDto getCurriculum(long id, Integer year, Integer semester)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Fetching curriculum of degree {} (year: {}, semester: {})", id, year, semester);

        Degree degree = degreeDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + id));

        if (year != null && (year < 1 || year > degree.getYears()))
            throw new MathtexpediaInvalidException("Year " + year + " is out of range for degree " + id + " (1.." + degree.getYears() + ")");
        if (semester != null && semester != 1 && semester != 2)
            throw new MathtexpediaInvalidException("Semester must be 1 or 2");

        return degreeMapper.toCurriculumDto(degree, year, degreeSubjectDataService.getByDegree(id, year, semester));
    }

    @Override
    public DegreeDto create(CreateDegreeDto dto) throws MathtexpediaConflictException {
        logger.info("Creating new degree with name: {}", dto.getName());

        Degree degree = degreeMapper.toEntity(dto);
        if (degreeDataService.existsByNameAndUniversity(degree.getName(), degree.getUniversity(), null))
            throw new MathtexpediaConflictException("Degree already exists: " + degree.getName());

        try {
            return degreeMapper.toDto(degreeDataService.create(degree));
        } catch (PersistenceException | DataIntegrityViolationException e) {
            throw new MathtexpediaConflictException("Error creating degree: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DegreeDto update(long id, UpdateDegreeDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating degree with id: {}", id);

        Degree toUpdate = degreeDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + id));

        String name = dto.getName().trim();
        String university = dto.getUniversity() == null || dto.getUniversity().isBlank() ? null : dto.getUniversity().trim();
        if (degreeDataService.existsByNameAndUniversity(name, university, id))
            throw new MathtexpediaConflictException("Degree already exists: " + name);

        boolean reducesYears = dto.getYears() < toUpdate.getYears();
        if (reducesYears) {
            // No se puede recortar el grado si hay asignaturas colocadas en los años que desaparecerían
            Optional<Integer> maxYear = degreeSubjectDataService.maxYearForDegree(id);
            if (maxYear.isPresent() && maxYear.get() > dto.getYears())
                throw new MathtexpediaConflictException("Cannot reduce degree " + id + " to " + dto.getYears()
                        + " years because it has subjects placed in year " + maxYear.get());
            // Los usuarios que estaban en un curso que desaparece se quedan sin curso (mantienen el grado)
            degreeDataService.clearUsersCurrentYearAbove(id, dto.getYears());
            // Y las suscripciones a esos cursos se borran: ya no hay donde colocar asignaturas nuevas
            courseSubscriptionDataService.removeAboveYear(id, dto.getYears());
        }

        degreeMapper.updateEntity(toUpdate, dto);
        try {
            return degreeMapper.toDto(degreeDataService.update(toUpdate));
        } catch (PersistenceException | DataIntegrityViolationException e) {
            throw new MathtexpediaConflictException("Error updating degree: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Deleting degree with id: {}", id);

        Degree toDelete = degreeDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + id));

        if (degreeSubjectDataService.countByDegree(id) > 0)
            throw new MathtexpediaConflictException("Cannot delete degree with id: " + id + " because it has subjects placed in it.");

        try {
            degreeDataService.delete(toDelete);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // Una asignatura se colocó en el grado justo entre la comprobación y el borrado: la FK lo impide
            throw new MathtexpediaConflictException("Error deleting degree with id: " + id, e);
        }
    }
}
