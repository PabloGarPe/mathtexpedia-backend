package mathtexpedia.es.api.domain.model.pdfRating;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PdfRatingSummaryDto {

    @Schema(description = "Número de votos positivos (👍)", example = "12")
    private long likeCount;

    @Schema(description = "Número de votos negativos (👎)", example = "2")
    private long dislikeCount;

    @Schema(description = "Límite inferior de Wilson de la proporción de 👍 (0..1), usado para ordenar. 0 si no hay votos",
            example = "0.69")
    private double score;

    /** Resumen de un PDF sin ningún voto. */
    public static PdfRatingSummaryDto empty() {
        return new PdfRatingSummaryDto(0, 0, 0.0);
    }
}
