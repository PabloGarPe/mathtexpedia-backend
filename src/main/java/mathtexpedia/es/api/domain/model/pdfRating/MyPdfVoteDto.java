package mathtexpedia.es.api.domain.model.pdfRating;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyPdfVoteDto {

    @Schema(description = "Id del PDF votado", example = "7")
    private long pdfId;

    @Schema(description = "Voto del usuario autenticado en ese PDF", example = "DISLIKE")
    private PdfVote vote;
}
