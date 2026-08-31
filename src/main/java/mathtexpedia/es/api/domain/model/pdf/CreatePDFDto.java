package mathtexpedia.es.api.domain.model.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePDFDto {

    @Schema(description = "Nombre único del PDF dentro del catálogo", example = "algebra-lineal-tema-1.pdf")
    @NotBlank
    private String name;

    @Schema(description = "Enlace de descarga del PDF")
    @NotBlank
    private String link;

    @Schema(description = "Identificador de la unidad temática a la que pertenece el PDF")
    @NotNull
    private Long subjectUnitId;

    @Schema(description = "Descripción opcional del contenido del PDF")
    private String description;

    @Override
    public String toString() {
        return "PDF [name=" + name + ", link=" + link + ", subjectUnitId=" + subjectUnitId + ", description=" + description + "]";
    }
}
