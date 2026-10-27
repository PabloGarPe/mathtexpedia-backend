package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.CommentContentException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaTooManyRequestsException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentConfigDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentContextDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentPageDto;
import mathtexpedia.es.api.domain.model.pdfComment.CreateCommentDto;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface PdfCommentService {

    /**
     * Encola un comentario nuevo ({@code PENDING}: lo revisa el worker de moderación). Responder a una respuesta cuelga de
     * la misma raíz y guarda a quién se contesta.
     *
     * @throws CommentContentException               el texto está vacío, es largo o tiene enlaces, imágenes o HTML
     * @throws MathtexpediaForbiddenException        el usuario no ha completado su perfil (sin username)
     * @throws MathtexpediaNotFoundException         no existe el PDF o el comentario al que se responde
     * @throws MathtexpediaInvalidException          el comentario al que se responde es de otro PDF
     * @throws MathtexpediaConflictException         el comentario al que se responde no está publicado
     * @throws MathtexpediaTooManyRequestsException  el usuario ya tiene el máximo de comentarios pendientes
     */
    CommentDto create(long pdfId, CreateCommentDto dto, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException,
            MathtexpediaInvalidException, MathtexpediaConflictException, MathtexpediaTooManyRequestsException;

    /** Raíces visibles del PDF, de la más reciente a la más antigua. Público. */
    CommentPageDto getRoots(long pdfId, int page, int size) throws MathtexpediaNotFoundException;

    /** Respuestas publicadas de una raíz, de la más antigua a la más reciente. Público. */
    CommentPageDto getReplies(long rootId, int page, int size) throws MathtexpediaNotFoundException;

    /**
     * Un comentario publicado junto a la raíz de su hilo, para enlazar a él sin paginar. Público.
     *
     * @throws MathtexpediaNotFoundException el comentario no existe o no está publicado
     */
    CommentContextDto getContext(long commentId) throws MathtexpediaNotFoundException;

    /** Límites que aplican a los comentarios (para que el front no los repita a mano). Público. */
    CommentConfigDto getConfig();

    /** Todos los comentarios del usuario en el PDF salvo los borrados, con su estado (pendiente, en revisión, rechazado…). */
    List<CommentDto> getMine(long pdfId, UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException;

    /** Borra el comentario (idempotente). Solo su autor o un admin. */
    void delete(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException;
}
