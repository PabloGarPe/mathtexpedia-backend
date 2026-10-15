package mathtexpedia.es.api.service.subject;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.event.ResourceDeletedEvent;
import mathtexpedia.es.api.domain.event.SubjectCreatedEvent;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.notification.ResourceType;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.infrastructure.security.CurrentUser;
import mathtexpedia.es.api.persistence.pdf.PDFDataService;
import mathtexpedia.es.api.persistence.quiz.QuizDataService;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements SubjectService{

    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;
    private final PDFDataService pdfDataService;
    private final QuizDataService quizDataService;
    private final SubjectMapper subjectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUser currentUser;

    public SubjectServiceImpl(
            SubjectDataService subjectDataService,
            SubjectUnitDataService subjectUnitDataService,
            PDFDataService pdfDataService,
            QuizDataService quizDataService,
            SubjectMapper subjectMapper,
            ApplicationEventPublisher eventPublisher,
            CurrentUser currentUser
    ) {
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
        this.pdfDataService = pdfDataService;
        this.quizDataService = quizDataService;
        this.subjectMapper = subjectMapper;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Override
    public List<SubjectDto> getSubjects() {
        logger.info("Fetching all subjects");

        return subjectDataService.getAll()
                .stream()
                .map(subjectMapper::toDto)
                .toList();
    }

    @Override
    public Optional<SubjectDto> getSubject(long id) {
        logger.info("Fetching subject with id: {}", id);

        Optional<Subject> subject = subjectDataService.getById(id);
        return subject.map(subjectMapper::toDto);
    }

    @Override
    public SubjectDto create(CreateSubjectDto dto) throws MathtexpediaConflictException {
        logger.info("Creating new subject with name: {}", dto.getName());

        Subject subject = subjectMapper.toEntity(dto);
        try {
            Subject created = subjectDataService.create(subject);
            eventPublisher.publishEvent(new SubjectCreatedEvent(created.getId(), currentUser.externalIdOrNull()));
            return subjectMapper.toDto(created);
        } catch (PersistenceException e) {
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
    public SubjectDto update(long id, UpdateSubjectDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating subject with id: {}", id);

        Subject toUpdate = subjectDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + id));
        subjectMapper.updateEntity(toUpdate, dto);
        try {
            Subject updated = subjectDataService.update(toUpdate);
            return subjectMapper.toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating subject: " + e.getMessage(), e);
        }
    }
}
