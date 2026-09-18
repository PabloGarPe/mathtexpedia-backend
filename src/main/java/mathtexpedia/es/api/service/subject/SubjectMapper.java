package mathtexpedia.es.api.service.subject;

import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.persistence.subject.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {

    public Subject toEntity(CreateSubjectDto dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());
        return subject;
    }

    public void updateEntity(Subject target, UpdateSubjectDto dto) {
        target.setName(dto.getName());
        target.setDescription(dto.getDescription());
    }

    public SubjectDto toDto(Subject subject) {
        return new SubjectDto(subject.getId(), subject.getName(), subject.getDescription());
    }
}
