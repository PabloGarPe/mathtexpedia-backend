package mathtexpedia.es.api.domain.model.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;

@Data
@AllArgsConstructor
public class CurriculumSubjectDto {

    @Schema(description = "Asignatura del plan de estudios")
    private SubjectDto subject;

    @Schema(description = "Cuatrimestre (1 o 2). Nulo si la asignatura es anual o no se especifica", nullable = true, example = "1")
    private Integer semester;
}
