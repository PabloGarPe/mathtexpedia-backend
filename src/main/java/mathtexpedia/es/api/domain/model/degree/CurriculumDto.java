package mathtexpedia.es.api.domain.model.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CurriculumDto {

    @Schema(description = "Grado al que pertenece el plan de estudios")
    private DegreeDto degree;

    @Schema(description = "Años del plan de estudios en orden. Sin filtro de año incluye todos los años del grado, también los vacíos; "
            + "con filtro, solo el año pedido")
    private List<CurriculumYearDto> years;
}
