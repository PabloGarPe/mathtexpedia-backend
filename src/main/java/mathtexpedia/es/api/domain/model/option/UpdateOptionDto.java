package mathtexpedia.es.api.domain.model.option;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOptionDto {


    @Schema(description = "Texto de la opción")
    @NotBlank
    @NotNull
    private String text;

    @Schema(description = "Indica si la opción es correcta o no")
    private boolean isCorrect;

    @Schema(description = "Posición de la opción en la pregunta")
    private int position;

    @Schema(description = "Identificador de la pregunta a la que pertenece la opción")
    @NotNull
    private Long questionId;

}
