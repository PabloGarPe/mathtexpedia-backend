package mathtexpedia.es.api.persistence.pdfComment;

import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Persistencia de los comentarios de PDFs. Los cambios de estado son <b>transiciones atómicas condicionales</b>: solo se
 * aplican si el comentario sigue en un estado válido y devuelven si realmente cambió algo. Así el worker de moderación, el
 * autor, los admins y las denuncias no se pisan entre sí, y quien llama sabe si debe publicar el evento de la transición.
 * Las reglas de qué transiciones están permitidas las decide el servicio (parámetro {@code from}).
 */
public interface PdfCommentDataService {

    PdfComment create(PdfComment comment);

    Optional<PdfComment> findById(long id);

    /**
     * Pasa a {@code PUBLISHED}, limpiando la categoría de moderación. {@code publishedAt} solo se fija la primera vez, para
     * que restaurar un comentario ya publicado no lo cambie de posición en los listados.
     *
     * @return true si estaba en alguno de los estados {@code from} y se ha publicado
     */
    boolean publish(long id, Set<CommentStatus> from, Instant now);

    /** Pasa a {@code REJECTED} con la categoría dada. @return true si estaba en alguno de los estados {@code from} */
    boolean reject(long id, Set<CommentStatus> from, ModerationCategory category, Instant now);

    /** Pasa a {@code REVIEW} (cola de admin) con la categoría como pista. @return true si estaba en alguno de los estados {@code from} */
    boolean sendToReview(long id, Set<CommentStatus> from, ModerationCategory hint, Instant now);

    /** Oculta por denuncias: solo actúa sobre {@code PUBLISHED}. @return true si estaba publicado y se ha ocultado */
    boolean hide(long id);

    /** Pasa a {@code DELETED} y vacía el texto. Actúa sobre cualquier estado salvo {@code DELETED}. @return true si se ha borrado ahora */
    boolean markDeleted(long id, Instant now);

    /** Suma un intento fallido de evaluación, de forma atómica. @return el valor nuevo, o 0 si el comentario no existe */
    int incrementModerationAttempts(long id);
}
