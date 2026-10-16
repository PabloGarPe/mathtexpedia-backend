package mathtexpedia.es.api.domain.model.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Colocación de salida de una asignatura en un grado. */
@Data
@AllArgsConstructor
public class SubjectPlacementInfoDto {

    @Schema(description = "ID del grado", example = "1")
    private Long degreeId;

    @Schema(description = "Nombre del grado", example = "Grado en Matemáticas")
    private String degreeName;

    @Schema(description = "Universidad del grado (puede ser nula)", nullable = true, example = "Universidad de Sevilla")
    private String university;

    @Schema(description = "Año (curso) del grado en el que se cursa", example = "4")
    private int year;

    @Schema(description = "Cuatrimestre (1 o 2). Nulo si la asignatura es anual o no se especifica", nullable = true, example = "1")
    private Integer semester;
}
