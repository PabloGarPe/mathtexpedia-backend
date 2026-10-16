package mathtexpedia.es.api.domain.model.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CurriculumYearDto {

    @Schema(description = "Año (curso) del grado", example = "4")
    private int year;

    @Schema(description = "Asignaturas de ese año ordenadas por cuatrimestre (las anuales al final) y nombre. Vacía si no hay ninguna")
    private List<CurriculumSubjectDto> subjects;
}
