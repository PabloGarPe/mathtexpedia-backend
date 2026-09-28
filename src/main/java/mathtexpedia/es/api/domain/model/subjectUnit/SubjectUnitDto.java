package mathtexpedia.es.api.domain.model.subjectUnit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectUnitDto {

    @Schema(description = "Identificador interno del tema", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del tema")
    private String name;

    @Schema(description = "Posición del tema dentro del orden de la asignatura")
    private int position;

}
