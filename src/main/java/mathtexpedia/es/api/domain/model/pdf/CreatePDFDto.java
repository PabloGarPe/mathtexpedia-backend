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

    @Schema(description = "Identificador de la asignatura a la que pertenece el PDF")
    @NotNull
    private Long subjectId;

    @Schema(description = "Identificador de la unidad temática a la que pertenece el PDF. En caso de que el PDF sea general para toda la asignatura, este campo puede ser nulo.",
            nullable = true)
    private Long subjectUnitId;

    @Schema(description = "Descripción opcional del contenido del PDF")
    private String description;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PDF [name=").append(name)
                .append(", link=").append(link)
                .append(", description=").append(description)
                .append(", subjectId=").append(subjectId);
        if (subjectUnitId != null) {
            sb.append(", subjectUnitId=").append(subjectUnitId);
        }
        sb.append("]");

        return sb.toString();
    }
}
