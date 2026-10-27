package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdfComment.AdminCommentPageDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationStatusDto;

import java.util.Set;

/** Cola de moderación para administradores. El controller comprueba que el usuario es admin. */
public interface PdfCommentAdminService {

    /**
     * Comentarios en esos estados, del más antiguo al más reciente, con sus denuncias abiertas.
     *
     * @throws MathtexpediaInvalidException algún estado no es consultable (solo PENDING, REVIEW, HIDDEN y REJECTED)
     */
    AdminCommentPageDto getQueue(Set<CommentStatus> statuses, int page, int size) throws MathtexpediaInvalidException;

    /**
     * Publica el comentario (también restaura uno oculto o un falso positivo rechazado) y cierra sus denuncias. Solo avisa
     * de "comentario nuevo" si es la primera vez que se publica.
     *
     * @throws MathtexpediaNotFoundException el comentario no existe
     * @throws MathtexpediaConflictException está publicado o borrado
     */
    void approve(long commentId) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    /**
     * Rechaza el comentario con una categoría, cierra sus denuncias y avisa a su autor.
     *
     * @throws MathtexpediaNotFoundException el comentario no existe
     * @throws MathtexpediaConflictException ya está rechazado o borrado
     */
    void reject(long commentId, ModerationCategory category) throws MathtexpediaNotFoundException, MathtexpediaConflictException;

    /** Estado de la moderación automática y tamaño de las colas de espera. */
    ModerationStatusDto getModerationStatus();
}
