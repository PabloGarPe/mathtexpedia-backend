package mathtexpedia.es.api.domain.model.pdf;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;

@Data
@AllArgsConstructor
public class UpdatePDFDto {

    @Schema(description = "Nombre único del PDF dentro del catálogo", example = "CDI: Tema 3 - Integrales")
    @NotBlank
    private String name;

    @Schema(description = "Enlace de descarga del PDF", example = "https://drive.google.com/file/d/1a2b3c4d5e6f7g8h9i0j/view?usp=sharing")
    private String link;

    @Schema(description = "Descripción del contenido del PDF", example = "Este PDF contiene ejercicios resueltos de integrales para el tema 3 del curso de CDI.")
    private String description;

    @Schema(description = "Identificador de la unidad temática a la que pertenece el PDF")
    @NotNull
    private Long subjectUnitId;
}
