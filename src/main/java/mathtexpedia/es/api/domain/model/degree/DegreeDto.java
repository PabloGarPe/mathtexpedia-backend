package mathtexpedia.es.api.domain.model.degree;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DegreeDto {

    @Schema(description = "Identificador interno del grado", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del grado", example = "Grado en Matemáticas")
    private String name;

    @Schema(description = "Universidad en la que se cursa (texto libre, opcional)", nullable = true, example = "Universidad de Sevilla")
    private String university;

    @Schema(description = "Número de años (cursos) del grado. Los años son los enteros de 1 a este valor", example = "4")
    private int years;
}
