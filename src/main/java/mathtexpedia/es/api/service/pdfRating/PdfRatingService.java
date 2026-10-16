package mathtexpedia.es.api.service.pdfRating;

import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfRating.MyPdfVoteDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfRatingSummaryDto;
import mathtexpedia.es.api.domain.model.pdfRating.PdfVote;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface PdfRatingService {

    /** Resumen público de cada PDF pedido; los que no tienen votos aparecen con un resumen vacío. */
    Map<Long, PdfRatingSummaryDto> getSummaries(Collection<Long> pdfIds);

    /** Valoración del PDF con el voto del usuario y si puede votar. */
    PdfRatingDto getRating(long pdfId, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException;

    /**
     * Vota el PDF o cambia el voto anterior. Solo puede quien haya abierto el PDF y no sea su autor.
     *
     * @throws MathtexpediaForbiddenException si es autor del PDF o todavía no lo ha abierto
     */
    PdfRatingDto rate(long pdfId, PdfVote vote, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException, MathtexpediaForbiddenException;

    /** Idempotente: si no había voto no hace nada. Siempre permitido, aunque el usuario ya no pudiera votar. */
    void removeRating(long pdfId, UserProfile user)
            throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException;

    /** Votos del usuario en esos PDFs, en el orden pedido; solo los que ha votado. Máximo 100 ids. */
    List<MyPdfVoteDto> getMyVotes(List<Long> pdfIds, UserProfile user)
            throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException;
}
