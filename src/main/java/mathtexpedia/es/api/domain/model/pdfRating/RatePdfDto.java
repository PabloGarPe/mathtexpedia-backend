package mathtexpedia.es.api.domain.model.pdfRating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para votar un PDF. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatePdfDto {

    @Schema(description = "Voto: LIKE (👍) o DISLIKE (👎)", example = "LIKE")
    @NotNull
    private PdfVote vote;
}
