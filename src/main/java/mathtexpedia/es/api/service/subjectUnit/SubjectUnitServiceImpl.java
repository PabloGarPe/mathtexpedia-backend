package mathtexpedia.es.api.service.subjectUnit;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.domain.event.SubjectUnitCreatedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.domain.model.subjectUnit.CreateSubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.UpdateSubjectUnitDto;
import mathtexpedia.es.api.infrastructure.security.CurrentUser;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectUnitServiceImpl implements SubjectUnitService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SubjectUnitServiceImpl.class);

    private final SubjectUnitDataService subjectUnitDataService;
    private final SubjectDataService subjectDataService;
    private final PDFDataService pDFDataService;
    private final QuizDataService quizDataService;
    private final SubjectUnitMapper subjectUnitMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUser currentUser;

    public SubjectUnitServiceImpl(
            SubjectUnitDataService subjectUnitService,
            SubjectDataService subjectService,
            PDFDataService pDFDataService,
            QuizDataService quizDataService,
            SubjectUnitMapper subjectUnitMapper,
            ApplicationEventPublisher eventPublisher,
            CurrentUser currentUser
    ) {
        this.subjectUnitDataService = subjectUnitService;
        this.subjectDataService = subjectService;
        this.pDFDataService = pDFDataService;
        this.quizDataService = quizDataService;
        this.subjectUnitMapper = subjectUnitMapper;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Override
    public List<SubjectUnitDto> getSubjectsUnitsBySubjectId(long subjectId) throws MathtexpediaNotFoundException {
        logger.info("Getting subject units for subject with id: {}", subjectId);

        if (subjectDataService.getById(subjectId).isEmpty()) {
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);
        }

        return subjectUnitDataService.getAllForSubject(subjectId)
                .stream()
                .map(subjectUnitMapper::toDto)
                .toList();
    }

    @Override
    public Optional<SubjectUnitDto> getSubjectUnit(long id) {
        logger.info("Getting subject unit with id: {}", id);

        Optional<SubjectUnit> subjectUnit = subjectUnitDataService.getById(id);
        return subjectUnit.map(subjectUnitMapper::toDto);
    }

    @Override
    public SubjectUnitDto create(CreateSubjectUnitDto dto, long subjectId) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating subject unit: {}", dto);

        SubjectUnit subjectUnit = subjectUnitMapper.toEntity(dto);

        Optional<Subject> subject = subjectDataService.getById(subjectId);
        if (subject.isEmpty()) {
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);
        }

        subjectUnit.setSubject(subject.get());
        try {
            SubjectUnit created = subjectUnitDataService.create(subjectUnit);
            // Sin listener a propósito: los temas solo se notificarán a los suscritos a la asignatura (#70)
            eventPublisher.publishEvent(new SubjectUnitCreatedEvent(created.getId(), subjectId, currentUser.externalIdOrNull()));
            return subjectUnitMapper.toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating subject unit: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Deleting subject unit with id: {}", id);

        SubjectUnit toDelete = subjectUnitDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + id));

        if(!pDFDataService.getAllForSubjectUnit(id).isEmpty())
            throw new MathtexpediaConflictException("Cannot delete subject unit with id: " + id + " because it has associated PDFs.");

        if (!quizDataService.getAllBySubjectUnitId(id, false).isEmpty())
            throw new MathtexpediaConflictException("Cannot delete subject unit with id: " + id + " because it has associated quizzes.");

        subjectUnitDataService.delete(toDelete);
        eventPublisher.publishEvent(new ResourceDeletedEvent(ResourceType.SUBJECT_UNIT, id));
    }

    @Override
    public SubjectUnitDto update(long id, UpdateSubjectUnitDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating subject unit with id: {}", id);

        SubjectUnit toUpdate = subjectUnitDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + id));

        subjectUnitMapper.updateEntity(toUpdate, dto);
        try {
            SubjectUnit updated = subjectUnitDataService.update(toUpdate);
            return subjectUnitMapper.toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating subject unit: " + e.getMessage(), e);
        }
    }
}
