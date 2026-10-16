package mathtexpedia.es.api.presentation.pdfRating;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfRating.MyPdfVoteDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingDto;
import mathtexpedia.es.api.domain.model.pdfRating.RatePdfDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfRating.PdfRatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Valoraciones de PDFs", description = "Votos 👍/👎 a los PDFs. Requiere JWT, no requiere rol ADMIN. "
        + "Solo puede votar quien haya abierto el PDF y no sea su autor; nunca se expone quién votó qué")
@RestController
public class PdfRatingController extends GenericController {

    private final PdfRatingService pdfRatingService;

    public PdfRatingController(PdfRatingService pdfRatingService) {
        this.pdfRatingService = pdfRatingService;
    }

    @Operation(summary = "Valoración de un PDF y mi voto",
            description = "Resumen público de votos, el voto del usuario autenticado (nulo si no ha votado) y si puede votar ahora. "
                    + "Si no puede, 'cannotVoteReason' indica el motivo: AUTHOR (es autor del PDF) o NOT_VIEWED (todavía no lo ha abierto)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Valoración del PDF"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @GetMapping("/pdf/{pdfId}/rating")
    public ResponseEntity<PdfRatingDto> getRating(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called getRating for PDF {} by user {}", pdfId, user.getId());

        return ResponseEntity.ok(pdfRatingService.getRating(pdfId, user));
    }

    @Operation(summary = "Voto un PDF o cambio mi voto",
            description = "Idempotente: votar de nuevo sustituye el voto anterior. Solo puede votar quien haya abierto el PDF "
                    + "(GET /pdf/{pdfId}) al menos una vez y no sea su autor. Devuelve la valoración recalculada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voto guardado; valoración actualizada del PDF"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (voto ausente o distinto de LIKE/DISLIKE)"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "403", description = "El usuario es autor del PDF o todavía no lo ha abierto"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @PutMapping("/pdf/{pdfId}/rating")
    public ResponseEntity<PdfRatingDto> rate(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @RequestBody @Valid RatePdfDto body,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException, MathtexpediaForbiddenException {
        logger.debug("Called rate for PDF {} with {} by user {}", pdfId, body.getVote(), user.getId());

        return ResponseEntity.ok(pdfRatingService.rate(pdfId, body.getVote(), user));
    }

    @Operation(summary = "Retiro mi voto de un PDF",
            description = "Idempotente: devuelve 204 también si no había voto. Siempre permitido, incluso si el usuario ya no podría votar")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "El usuario ya no tiene voto en el PDF"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @DeleteMapping("/pdf/{pdfId}/rating")
    public ResponseEntity<Void> removeRating(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called removeRating for PDF {} by user {}", pdfId, user.getId());

        pdfRatingService.removeRating(pdfId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mis votos en un lote de PDFs",
            description = "Pensado para marcar los PDFs ya votados en un listado. Devuelve solo los PDFs que el usuario ha votado, "
                    + "en el orden pedido; los ids repetidos se ignoran. Máximo 100 ids")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Votos del usuario (puede estar vacía)"),
            @ApiResponse(responseCode = "400", description = "Más de 100 ids o ids mal formados"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva")
    })
    @GetMapping("/me/pdf-ratings")
    public ResponseEntity<List<MyPdfVoteDto>> getMyVotes(
            @Parameter(description = "Ids de los PDFs separados por comas", required = true, example = "1,2,3")
            @RequestParam List<Long> pdfIds,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called getMyVotes for {} PDFs by user {}", pdfIds.size(), user.getId());

        return ResponseEntity.ok(pdfRatingService.getMyVotes(pdfIds, user));
    }
}
