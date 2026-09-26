package mathtexpedia.es.api.domain.model.option;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptionDto {

    @Schema(description = "Identificador interno de la opción", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Texto de la opción")
    private String text;

    @Schema(description = "Indica si la opción es correcta o no")
    private boolean isCorrect;

    @Schema(description = "Posición de la opción en la pregunta")
    private int position;

    @Schema(description = "Identificador de la pregunta a la que pertenece la opción")
    private Long questionId;

}
