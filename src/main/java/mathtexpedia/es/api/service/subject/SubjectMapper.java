package mathtexpedia.es.api.service.subject;

import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDetailDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectPlacementDto;
import mathtexpedia.es.api.domain.model.subject.SubjectPlacementInfoDto;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.persistence.subject.Subject;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubject;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public SubjectDetailDto toDetailDto(Subject subject, List<DegreeSubject> placements) {
        List<SubjectPlacementInfoDto> placementDtos = placements.stream()
                .map(placement -> new SubjectPlacementInfoDto(
                        placement.getDegree().getId(),
                        placement.getDegree().getName(),
                        placement.getDegree().getUniversity(),
                        placement.getYear(),
                        placement.getSemester()))
                .toList();
        return new SubjectDetailDto(subject.getId(), subject.getName(), subject.getDescription(), placementDtos);
    }

    /** Colocación sin asignatura todavía: {@code DegreeSubjectDataService.replaceForSubject} se la asigna al guardarla. */
    public DegreeSubject toPlacementEntity(SubjectPlacementDto dto, Degree degree) {
        DegreeSubject placement = new DegreeSubject();
        placement.setDegree(degree);
        placement.setYear(dto.getYear());
        placement.setSemester(dto.getSemester());
        return placement;
    }
}
