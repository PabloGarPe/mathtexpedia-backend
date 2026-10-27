package mathtexpedia.es.api.persistence.pdfCommentLike;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface PdfCommentLikeDataService {

    /** Crea el like si no existía, sin fallar ante dos peticiones simultáneas del mismo usuario. @return true si lo ha creado */
    boolean insertIfAbsent(long commentId, long userId, Instant now);

    /** @return true si existía y se ha borrado */
    boolean delete(long commentId, long userId);

    /** Likes de cada comentario. Los comentarios sin likes no aparecen en el mapa. */
    Map<Long, Long> countByComment(Collection<Long> commentIds);

    /** De esos comentarios, los que le gustan al usuario. */
    Set<Long> findLikedIds(long userId, Collection<Long> commentIds);
}
