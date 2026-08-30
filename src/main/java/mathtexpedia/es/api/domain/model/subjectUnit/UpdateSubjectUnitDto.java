package mathtexpedia.es.api.domain.model.subjectUnit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSubjectUnitDto {

    @Schema(description = "Nombre único del tema dentro de la asignatura", example = "Tema 1: Límites y Continuidad")
    @NotBlank
    private String name;

    @Schema(description = "Posición del tema dentro del orden de la asignatura", example = "1")
    private int position;
}
