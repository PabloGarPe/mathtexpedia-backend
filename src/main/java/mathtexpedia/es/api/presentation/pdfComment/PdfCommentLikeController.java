package mathtexpedia.es.api.presentation.pdfComment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentLikeDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfComment.PdfCommentLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Likes de comentarios", description = "Me gusta en los comentarios. Requiere JWT, no requiere rol ADMIN. "
        + "Un usuario cuenta una sola vez por comentario; nunca se expone quién ha dado like")
@RestController
public class PdfCommentLikeController extends GenericController {

    private final PdfCommentLikeService likeService;

    public PdfCommentLikeController(PdfCommentLikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "Doy like a un comentario",
            description = "Idempotente: repetirlo no suma más likes. Solo comentarios publicados y que no sean del propio usuario. "
                    + "Devuelve el número de likes actualizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Like guardado; likes actualizados"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "403", description = "Es un comentario del propio usuario"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id"),
            @ApiResponse(responseCode = "409", description = "El comentario no está publicado")
    })
    @PutMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeDto> like(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaForbiddenException,
            MathtexpediaConflictException {
        logger.debug("Called like comment {} by user {}", commentId, user.getId());

        return ResponseEntity.ok(likeService.like(commentId, user));
    }

    @Operation(summary = "Quito mi like de un comentario",
            description = "Idempotente: también devuelve 200 si no había like. Siempre permitido, aunque el comentario ya no esté publicado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El usuario ya no tiene like en el comentario; likes actualizados"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id")
    })
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentLikeDto> unlike(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called unlike comment {} by user {}", commentId, user.getId());

        return ResponseEntity.ok(likeService.unlike(commentId, user));
    }

    @Operation(summary = "Mis likes en un lote de comentarios",
            description = "Pensado para marcar los comentarios que ya me gustan en un listado (que es público y no sabe quién mira). "
                    + "Devuelve solo los ids que le gustan al usuario, en el orden pedido. Máximo 100 ids")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ids de comentarios que le gustan al usuario (puede estar vacía)"),
            @ApiResponse(responseCode = "400", description = "Más de 100 ids o ids mal formados"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva")
    })
    @GetMapping("/me/comment-likes")
    public ResponseEntity<List<Long>> getMyLikes(
            @Parameter(description = "Ids de los comentarios separados por comas", required = true, example = "1,2,3")
            @RequestParam List<Long> commentIds,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.debug("Called getMyLikes for {} comments by user {}", commentIds.size(), user.getId());

        return ResponseEntity.ok(likeService.getMyLikes(commentIds, user));
    }
}
