package mathtexpedia.es.api.domain.model.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePDFDto {

    @Schema(description = "Nombre único del PDF dentro del catálogo", example = "CDI: Tema 3 - Integrales")
    @NotBlank
    private String name;

    @Schema(description = "Descripción del contenido del PDF", example = "Este PDF contiene ejercicios resueltos de integrales para el tema 3 del curso de CDI.")
    private String description;

    @Schema(description = "Identificador de la asignatura a la que pertenece el PDF")
    @NotNull
    private Long subjectId;

    @Schema(description = "Identificador de la unidad temática a la que pertenece el PDF. En caso de que el PDF sea general para toda la asignatura, este campo puede ser nulo.", nullable = true)
    private Long subjectUnitId;
}