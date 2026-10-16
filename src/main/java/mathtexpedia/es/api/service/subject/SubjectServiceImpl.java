package mathtexpedia.es.api.service.subject;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.domain.event.SubjectCreatedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDetailDto;
import mathtexpedia.es.api.domain.model.subject.SubjectFilter;
import mathtexpedia.es.api.domain.model.subject.SubjectPlacementDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.infrastructure.security.CurrentUser;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.degree.DegreeDataService;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubject;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubjectDataService;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SubjectServiceImpl implements SubjectService{

    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final PDFDataService pdfDataService;
    private final QuizDataService quizDataService;
    private final DegreeDataService degreeDataService;
    private final DegreeSubjectDataService degreeSubjectDataService;
    private final SubjectMapper subjectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUser currentUser;

    public SubjectServiceImpl(
            SubjectDataService subjectDataService,
            SubjectUnitDataService subjectUnitDataService,
            PDFDataService pdfDataService,
            QuizDataService quizDataService,
            DegreeDataService degreeDataService,
            DegreeSubjectDataService degreeSubjectDataService,
            SubjectMapper subjectMapper,
            ApplicationEventPublisher eventPublisher,
            CurrentUser currentUser
    ) {
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.pdfDataService = pdfDataService;
        this.quizDataService = quizDataService;
        this.degreeDataService = degreeDataService;
        this.degreeSubjectDataService = degreeSubjectDataService;
        this.subjectMapper = subjectMapper;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Override
    public List<SubjectDetailDto> getSubjects(SubjectFilter filter) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Fetching subjects with filter: {}", filter);

        validateFilter(filter);

        List<Subject> subjects = subjectDataService.getFiltered(filter.degreeId(), filter.year(), filter.semester(), filter.unassigned());

        // Una sola consulta para las colocaciones de todas las asignaturas, agrupadas en memoria (sin N+1)
        Map<Long, List<DegreeSubject>> placementsBySubject = new LinkedHashMap<>();
        degreeSubjectDataService.getBySubjectIds(subjects.stream().map(Subject::getId).toList())
                .forEach(placement -> placementsBySubject
                        .computeIfAbsent(placement.getSubject().getId(), key -> new ArrayList<>())
                        .add(placement));

        return subjects.stream()
                .map(subject -> subjectMapper.toDetailDto(subject, placementsBySubject.getOrDefault(subject.getId(), List.of())))
                .toList();
    }

    @Override
    public Optional<SubjectDetailDto> getSubject(long id) {
        logger.info("Fetching subject with id: {}", id);

        return subjectDataService.getById(id)
                .map(subject -> subjectMapper.toDetailDto(subject, degreeSubjectDataService.getBySubjectIds(List.of(id))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectDetailDto create(CreateSubjectDto dto)
            throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.info("Creating new subject with name: {}", dto.getName());

        List<DegreeSubject> placements = buildPlacements(dto.getPlacements());

        Subject subject = subjectMapper.toEntity(dto);
        try {
            Subject created = subjectDataService.create(subject);
            if (!placements.isEmpty())
                degreeSubjectDataService.replaceForSubject(created.getId(), placements);
            // El listener de notificaciones es AFTER_COMMIT: cuando corra, las colocaciones ya estarán en base de datos
            eventPublisher.publishEvent(new SubjectCreatedEvent(created.getId(), currentUser.externalIdOrNull()));
            return subjectMapper.toDetailDto(created, placements);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            throw new MathtexpediaConflictException("Error creating subject: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Deleting subject with id: {}", id);

        Subject toDelete = subjectDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + id));

        if (!subjectUnitDataService.getAllForSubject(id).isEmpty())
            throw new MathtexpediaConflictException("Cannot delete subject with id: " + id + " because it has associated subject units.");

        if (!pdfDataService.getGeneralPDFsForSubject(id).isEmpty())
            throw new MathtexpediaConflictException("Cannot delete subject with id: " + id + " because it has associated PDFs.");

        if (!quizDataService.getAllBySubjectId(id, false).isEmpty())
            throw new MathtexpediaConflictException("Cannot delete subject with id: " + id + " because it has associated quizzes.");

        subjectDataService.delete(toDelete);
        eventPublisher.publishEvent(new ResourceDeletedEvent(ResourceType.SUBJECT, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubjectDetailDto update(long id, UpdateSubjectDto dto)
            throws MathtexpediaNotFoundException, MathtexpediaConflictException, MathtexpediaInvalidException {
        logger.info("Updating subject with id: {}", id);

        Subject toUpdate = subjectDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + id));

        // Nulo conserva las colocaciones actuales; una lista (también vacía) las reemplaza todas
        List<DegreeSubject> newPlacements = dto.getPlacements() == null ? null : buildPlacements(dto.getPlacements());

        subjectMapper.updateEntity(toUpdate, dto);
        try {
            Subject updated = subjectDataService.update(toUpdate);
            if (newPlacements != null)
                degreeSubjectDataService.replaceForSubject(id, newPlacements);
            List<DegreeSubject> placements = newPlacements != null ? newPlacements : degreeSubjectDataService.getBySubjectIds(List.of(id));
            return subjectMapper.toDetailDto(updated, placements);
        } catch (PersistenceException | DataIntegrityViolationException e) {
            throw new MathtexpediaConflictException("Error updating subject: " + e.getMessage(), e);
        }
    }

    /** Valida las combinaciones de filtros del listado y que el grado filtrado exista. */
    private void validateFilter(SubjectFilter filter) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        if (filter.unassigned() && filter.degreeId() != null)
            throw new MathtexpediaInvalidException("Parameter unassigned cannot be combined with degreeId");
        if (filter.degreeId() == null && (filter.year() != null || filter.semester() != null))
            throw new MathtexpediaInvalidException("Parameters year and semester require degreeId");
        if (filter.year() != null && filter.year() < 1)
            throw new MathtexpediaInvalidException("Year must be greater than or equal to 1");
        if (filter.semester() != null && filter.semester() != 1 && filter.semester() != 2)
            throw new MathtexpediaInvalidException("Semester must be 1 or 2");

        if (filter.degreeId() != null && degreeDataService.getById(filter.degreeId()).isEmpty())
            throw new MathtexpediaNotFoundException("Degree not found with id: " + filter.degreeId());
    }

    /**
     * Valida las colocaciones recibidas y las convierte en entidades: cada grado debe existir (404), no repetirse (400) y tener
     * al menos tantos años como el de la colocación (400). Nulo equivale a ninguna colocación.
     */
    private List<DegreeSubject> buildPlacements(List<SubjectPlacementDto> placements)
            throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        List<DegreeSubject> result = new ArrayList<>();
        if (placements == null)
            return result;

        Set<Long> seenDegrees = new HashSet<>();
        for (SubjectPlacementDto placement : placements) {
            if (!seenDegrees.add(placement.getDegreeId()))
                throw new MathtexpediaInvalidException("Degree " + placement.getDegreeId() + " appears more than once in placements");

            Degree degree = degreeDataService.getById(placement.getDegreeId())
                    .orElseThrow(() -> new MathtexpediaNotFoundException("Degree not found with id: " + placement.getDegreeId()));

            if (placement.getYear() > degree.getYears())
                throw new MathtexpediaInvalidException("Year " + placement.getYear() + " is out of range for degree "
                        + degree.getId() + " (1.." + degree.getYears() + ")");

            result.add(subjectMapper.toPlacementEntity(placement, degree));
        }
        // Mismo orden que devuelve la consulta de colocaciones, para que la respuesta no dependa del orden de entrada
        result.sort(Comparator.comparing((DegreeSubject placement) -> placement.getDegree().getName())
                .thenComparingInt(DegreeSubject::getYear));
        return result;
    }
}
