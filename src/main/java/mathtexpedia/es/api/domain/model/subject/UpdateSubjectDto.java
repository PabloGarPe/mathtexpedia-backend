package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSubjectDto {

    @Schema(description = "Nombre único de la asignatura dentro del catálogo", example = "Cálculo Diferencial e Integral")
    @NotBlank
    private String name;

    @Schema(description = "Descripción opcional de la asignatura")
    @Size(max = 500)
    private String description;
}
