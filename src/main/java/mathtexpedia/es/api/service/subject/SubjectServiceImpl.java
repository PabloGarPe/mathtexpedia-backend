package mathtexpedia.es.api.service.subject;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectServiceImpl implements SubjectService{

    private static final Logger logger = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private final SubjectDataService subjectDataService;
    private final SubjectUnitDataService subjectUnitDataService;

    public SubjectServiceImpl(SubjectDataService subjectDataService, SubjectUnitDataService subjectUnitDataService) {
        this.subjectDataService = subjectDataService;
        this.subjectUnitDataService = subjectUnitDataService;
    }

    @Override
    public List<SubjectDto> getSubjects() {
        logger.info("Fetching all subjects");

        return subjectDataService.getAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<SubjectDto> getSubject(long id) {
        logger.info("Fetching subject with id: {}", id);

        Optional<Subject> subject = subjectDataService.getById(id);
        return subject.map(this::toDto);
    }

    @Override
    public SubjectDto create(CreateSubjectDto dto) throws MathtexpediaConflictException {
        logger.info("Creating new subject with name: {}", dto.getName());

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        try {
            Subject created = subjectDataService.create(subject);
            return toDto(created);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error creating subject: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Deleting subject with id: {}", id);

        Subject toDelete = subjectDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + id));

        if (!subjectUnitDataService.getAllForSubject(id).isEmpty()) {
            throw new MathtexpediaConflictException("Cannot delete subject with id: " + id + " because it has associated subject units.");
        }

        subjectDataService.delete(toDelete);
    }

    @Override
    public SubjectDto update(long id, UpdateSubjectDto dto) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Updating subject with id: {}", id);

        Subject toUpdate = subjectDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject not found with id: " + id));
        toUpdate.setName(dto.getName());
        toUpdate.setDescription(dto.getDescription());
        try {
            Subject updated = subjectDataService.update(toUpdate);
            return toDto(updated);
        } catch (PersistenceException e) {
            throw new MathtexpediaConflictException("Error updating subject: " + e.getMessage(), e);
        }
    }

    private SubjectDto toDto(Subject subject) {
        return new SubjectDto(subject.getId(), subject.getName(), subject.getDescription());
    }
}
