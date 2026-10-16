package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateSubjectDto {

    @Schema(description = "Nombre único de la asignatura dentro del catálogo", example = "Cálculo Diferencial e Integral")
    @NotBlank
    private String name;

    @Schema(description = "Descripción opcional de la asignatura")
    @Size(max = 500)
    private String description;

    @Schema(description = "Colocaciones de la asignatura en grados (opcional). En el alta, nulo o vacío deja la asignatura sin colocaciones", nullable = true)
    @Valid
    private List<SubjectPlacementDto> placements;
}
