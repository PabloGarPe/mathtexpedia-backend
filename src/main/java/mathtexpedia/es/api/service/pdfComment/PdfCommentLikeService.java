package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentLikeDto;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface PdfCommentLikeService {

    /**
     * Da like a un comentario publicado. Idempotente: un usuario solo cuenta una vez por comentario, aunque repita la petición.
     *
     * @throws MathtexpediaNotFoundException  el comentario no existe
     * @throws MathtexpediaForbiddenException es un comentario del propio usuario
     * @throws MathtexpediaConflictException  el comentario no está publicado
     */
    CommentLikeDto like(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaForbiddenException,
            MathtexpediaConflictException;

    /**
     * Quita el like (idempotente). Siempre permitido, aunque el comentario ya no esté publicado.
     *
     * @throws MathtexpediaNotFoundException el comentario no existe
     */
    CommentLikeDto unlike(long commentId, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException;

    /**
     * Ids, de los pedidos, de los comentarios que le gustan al usuario.
     *
     * @throws MathtexpediaInvalidException más de 100 ids
     */
    List<Long> getMyLikes(List<Long> commentIds, UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException;
}
