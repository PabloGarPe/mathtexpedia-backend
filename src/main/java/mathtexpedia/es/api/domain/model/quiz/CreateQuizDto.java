package mathtexpedia.es.api.domain.model.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateQuizDto {

    @Schema(description = "Nombre del cuestionario", example = "Cuestionario de Álgebra Lineal")
    @NotBlank
    @NotNull
    private String name;

    @Schema(description = "Descripción del cuestionario",
            example = "Cuestionario sobre operaciones con matrices",
            nullable = true)
    private String description;

    @Schema(description = "Dificultad del cuestionario", example = "EASY|MEDIUM|HARD")
    @NotNull
    private Difficulty difficulty;

    @Schema(description = "Identificador de la asignatura a la que pertenece el cuestionario", example = "1")
    @NotNull
    private Long subjectId;

    @Schema(description = "Identificador del tema al que pertenece el cuestionario",
            example = "1",
            nullable = true)
    private Long subjectUnitId;
}
