package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Asignatura con sus colocaciones en grados. {@link SubjectDto} no las incluye porque va embebido en otros DTOs. */
@Data
@AllArgsConstructor
public class SubjectDetailDto {

    @Schema(description = "Identificador interno de la asignatura", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre único de la asignatura")
    private String name;

    @Schema(description = "Descripción opcional de la asignatura")
    private String description;

    @Schema(description = "Grados en los que está colocada la asignatura. Lista vacía si no está en ninguno")
    private List<SubjectPlacementInfoDto> placements;
}
