package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.pdfComment.PdfComment;

import java.util.Optional;

public interface PdfCommentModerationService {

    Optional<PdfComment> nextPending();

    boolean publish(long commentId);

    boolean reject(long commentId, ModerationCategory category);

    boolean sendToReview(long commentId, ModerationCategory hint);

    /**
     * Cuenta un intento de evaluación fallido (timeout o salida inválida del modelo). Al llegar a
     * {@code moderation.max-attempts} lo manda a revisión de un admin para que no bloquee la cola.
     *
     * @return true si con este intento se ha enviado a revisión
     */
    boolean registerFailedAttempt(long commentId);
}
