package mathtexpedia.es.api.domain.model.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Datos para actualizar un grado (reemplaza todos los campos). */
@Data
public class UpdateDegreeDto {

    @Schema(description = "Nombre del grado. Único, sin distinguir mayúsculas, junto con la universidad", example = "Grado en Matemáticas")
    @NotBlank
    @Size(max = 150)
    private String name;

    @Schema(description = "Universidad en la que se cursa (opcional). Nulo o vacío lo borra", nullable = true, example = "Universidad de Sevilla")
    @Size(max = 150)
    private String university;

    @Schema(description = "Número de años (cursos) del grado, entre 1 y 10", example = "4")
    @NotNull
    @Min(1)
    @Max(10)
    private Integer years;
}
