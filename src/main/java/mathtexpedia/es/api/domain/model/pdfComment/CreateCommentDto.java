package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentDto {

    @NotNull
    @Schema(description = "Texto en Markdown con fórmulas LaTeX ($...$). Sin enlaces, imágenes ni HTML. Máximo 1000 caracteres",
            example = "¿Alguien sabe por qué en la página 12 se puede intercambiar el límite con la integral?")
    private String body;

    @Schema(description = "Id del comentario al que se responde (una raíz o una respuesta, ya publicado y del mismo PDF). "
            + "Nulo para un comentario nuevo", nullable = true)
    private Long replyToId;
}
