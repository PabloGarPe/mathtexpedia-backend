package mathtexpedia.es.api.service.degree;

import mathtexpedia.es.api.domain.model.degree.CreateDegreeDto;
import mathtexpedia.es.api.domain.model.degree.CurriculumDto;
import mathtexpedia.es.api.domain.model.degree.CurriculumSubjectDto;
import mathtexpedia.es.api.domain.model.degree.CurriculumYearDto;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;
import mathtexpedia.es.api.domain.model.degree.UpdateDegreeDto;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.degreeSubject.DegreeSubject;
import mathtexpedia.es.api.service.subject.SubjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class DegreeMapper {

    private final SubjectMapper subjectMapper;

    public DegreeMapper(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    public Degree toEntity(CreateDegreeDto dto) {
        Degree degree = new Degree();
        degree.setName(dto.getName().trim());
        degree.setUniversity(blankToNull(dto.getUniversity()));
        degree.setYears(dto.getYears());
        return degree;
    }

    public void updateEntity(Degree target, UpdateDegreeDto dto) {
        target.setName(dto.getName().trim());
        target.setUniversity(blankToNull(dto.getUniversity()));
        target.setYears(dto.getYears());
    }

    public DegreeDto toDto(Degree degree) {
        return new DegreeDto(degree.getId(), degree.getName(), degree.getUniversity(), degree.getYears());
    }

    /**
     * Agrupa las colocaciones por año. Sin {@code year} incluye todos los años del grado (también los vacíos); con {@code year},
     * solo ese. Las colocaciones deben venir ya ordenadas por año, cuatrimestre y nombre.
     */
    public CurriculumDto toCurriculumDto(Degree degree, Integer year, List<DegreeSubject> placements) {
        List<Integer> years = year != null ? List.of(year) : IntStream.rangeClosed(1, degree.getYears()).boxed().toList();

        List<CurriculumYearDto> curriculumYears = new ArrayList<>();
        for (int y : years) {
            List<CurriculumSubjectDto> subjects = placements.stream()
                    .filter(placement -> placement.getYear() == y)
                    .map(placement -> new CurriculumSubjectDto(subjectMapper.toDto(placement.getSubject()), placement.getSemester()))
                    .toList();
            curriculumYears.add(new CurriculumYearDto(y, subjects));
        }
        return new CurriculumDto(toDto(degree), curriculumYears);
    }

    private static String blankToNull(String value) {
        if (value == null)
            return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
