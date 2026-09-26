package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectDto {

    @Schema(description = "Identificador interno de la asignatura", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre único de la asignatura")
    private String name;

    @Schema(description = "Descripción opcional de la asignatura")
    private String description;
}
