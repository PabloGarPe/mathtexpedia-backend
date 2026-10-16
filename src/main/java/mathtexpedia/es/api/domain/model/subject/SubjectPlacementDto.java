package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Colocación de entrada de una asignatura en un grado, dentro del alta o la edición de la asignatura. */
@Data
public class SubjectPlacementDto {

    @Schema(description = "ID del grado en el que se coloca la asignatura. Un mismo grado no puede repetirse en la lista", example = "1")
    @NotNull
    private Long degreeId;

    @Schema(description = "Año (curso) del grado en el que se cursa, entre 1 y los años del grado", example = "4")
    @NotNull
    @Min(1)
    private Integer year;

    @Schema(description = "Cuatrimestre: 1 o 2. Nulo si la asignatura es anual o no se especifica", nullable = true, example = "1")
    @Min(1)
    @Max(2)
    private Integer semester;
}
