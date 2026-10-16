package mathtexpedia.es.api.persistence.pdfRating;

import mathtexpedia.es.api.domain.model.pdfRating.PdfVote;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PdfRatingDataService {

    /** Crea el voto o cambia el que ya había, sin fallar ante dos peticiones simultáneas del mismo usuario. */
    void upsert(long pdfId, long userAccountId, PdfVote vote);

    /** @return true si existía y se ha borrado */
    boolean delete(long pdfId, long userAccountId);

    /** Borra los votos de esos usuarios en el PDF (no hace nada si la colección está vacía). @return los borrados */
    int deleteByUsers(long pdfId, Collection<Long> userAccountIds);

    Optional<PdfVote> getVote(long pdfId, long userAccountId);

    /** Votos del usuario en esos PDFs, por id de PDF. Solo aparecen los que ha votado. */
    Map<Long, PdfVote> getVotes(long userAccountId, Collection<Long> pdfIds);

    /** Número de 👍 y 👎 de cada PDF. Los PDFs sin votos no aparecen en el mapa. */
    Map<Long, PdfVoteCounts> countVotes(Collection<Long> pdfIds);

    /**
     * Ids de los PDFs mejor valorados: con al menos {@code minVotes} votos y más 👍 que 👎, ordenados por el límite inferior
     * de Wilson de la proporción de 👍 (de mayor a menor), luego por número de 👍 y por id.
     */
    List<Long> getTopRatedPdfIds(int minVotes, int limit);
}
