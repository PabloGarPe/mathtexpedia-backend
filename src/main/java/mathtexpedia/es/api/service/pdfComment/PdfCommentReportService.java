package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;
import mathtexpedia.es.api.domain.security.UserProfile;

public interface PdfCommentReportService {

    /**
     * Denuncia un comentario publicado. Idempotente por usuario. Al llegar al umbral de denunciantes distintos el comentario
     * se oculta ({@code HIDDEN}) y queda a la espera de un admin. Nunca se revela quién denuncia.
     *
     * @throws MathtexpediaNotFoundException  el comentario no existe
     * @throws MathtexpediaForbiddenException es un comentario del propio usuario
     * @throws MathtexpediaConflictException  el comentario no está publicado
     */
    void report(long commentId, ReportReason reason, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaForbiddenException,
            MathtexpediaConflictException;
}
