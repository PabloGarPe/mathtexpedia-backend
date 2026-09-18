package mathtexpedia.es.api.service.subjectUnit;

import mathtexpedia.es.api.domain.model.subjectUnit.CreateSubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.UpdateSubjectUnitDto;
import mathtexpedia.es.api.persistence.subjectUnit.SubjectUnit;
import org.springframework.stereotype.Component;

@Component
public class SubjectUnitMapper {

    public SubjectUnit toEntity(CreateSubjectUnitDto dto) {
        SubjectUnit subjectUnit = new SubjectUnit();
        subjectUnit.setName(dto.getName());
        subjectUnit.setPosition(dto.getPosition());
        return subjectUnit;
    }

    public void updateEntity(SubjectUnit target, UpdateSubjectUnitDto dto) {
        target.setName(dto.getName());
        target.setPosition(dto.getPosition());
    }

    public SubjectUnitDto toDto(SubjectUnit subjectUnit) {
        return new SubjectUnitDto(subjectUnit.getId(), subjectUnit.getName(), subjectUnit.getPosition());
    }
}
