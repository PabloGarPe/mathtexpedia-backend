package mathtexpedia.es.api.domain.model.pdfRating;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PdfRatingDto {

    @Schema(description = "Id del PDF", example = "7")
    private long pdfId;

    @Schema(description = "Resumen público de valoraciones del PDF")
    private PdfRatingSummaryDto rating;

    @Schema(description = "Voto del usuario autenticado. Nulo si no ha votado", nullable = true, example = "LIKE")
    private PdfVote myVote;

    @Schema(description = "Si el usuario autenticado puede votar (o cambiar su voto) ahora mismo en este PDF", example = "true")
    private boolean canVote;

    @Schema(description = "Por qué no puede votar. Nulo si puede. AUTHOR (es autor del PDF) tiene prioridad sobre NOT_VIEWED (no lo ha abierto todavía)",
            nullable = true, example = "NOT_VIEWED")
    private CannotVoteReason cannotVoteReason;
}
