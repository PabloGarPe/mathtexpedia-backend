package mathtexpedia.es.api.service.subjectUnit;

import jakarta.persistence.PersistenceException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.subjectUnit.CreateSubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.UpdateSubjectUnitDto;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.subject.SubjectDataService;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnitDataService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectUnitServiceImpl implements SubjectUnitService{

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SubjectUnitServiceImpl.class);

    private final SubjectUnitDataService subjectUnitDataService;
    private final SubjectDataService subjectDataService;

    public SubjectUnitServiceImpl(SubjectUnitDataService subjectUnitService, SubjectDataService subjectService) {
        this.subjectUnitDataService = subjectUnitService;
        this.subjectDataService = subjectService;
    }

    @Override
    public List<SubjectUnitDto> getSubjectsUnitsBySubjectId(long subjectId) {
        logger.info("Getting subject units for subject with id: {}", subjectId);

        return subjectUnitDataService.getAllForSubject(subjectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Optional<SubjectUnitDto> getSubjectUnit(long id) {
        logger.info("Getting subject unit with id: {}", id);

        Optional<SubjectUnit> subjectUnit = subjectUnitDataService.getById(id);
        return subjectUnit.map(this::toDto);
    }

    @Override
    public SubjectUnitDto create(CreateSubjectUnitDto dto, long subjectId) throws MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.info("Creating subject unit: {}", dto);

        SubjectUnit subjectUnit = new SubjectUnit();
        subjectUnit.setName(dto.getName());
        subjectUnit.setPosition(dto.getPosition());

        Optional<Subject> subject = subjectDataService.getById(subjectId);
        if (subject.isPresent()) {
            subjectUnit.setSubject(subject.get());
            try {
                SubjectUnit created = subjectUnitDataService.create(subjectUnit);
                return toDto(created);
            } catch (PersistenceException e) {
                throw new MathtexpediaConflictException("Error creating subject unit: " + e.getMessage(), e);
            }
        } else {
            throw new MathtexpediaNotFoundException("Subject not found with id: " + subjectId);
        }
    }

    @Override
    public void delete(long id) throws MathtexpediaNotFoundException {
        logger.info("Deleting subject unit with id: {}", id);

        SubjectUnit toDelete = subjectUnitDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + id));
        subjectUnitDataService.delete(toDelete);
    }

    @Override
    public SubjectUnitDto update(long id, UpdateSubjectUnitDto dto) throws MathtexpediaNotFoundException {
        logger.info("Updating subject unit with id: {}", id);

        SubjectUnit toUpdate = subjectUnitDataService.getById(id)
                .orElseThrow(() -> new MathtexpediaNotFoundException("Subject unit not found with id: " + id));

        toUpdate.setName(dto.getName());
        toUpdate.setPosition(dto.getPosition());

        SubjectUnit updated = subjectUnitDataService.update(toUpdate);
        return toDto(updated);
    }

    private SubjectUnitDto toDto(SubjectUnit subjectUnit) {
        return new SubjectUnitDto(subjectUnit.getId(), subjectUnit.getName(), subjectUnit.getPosition());
    }
}
