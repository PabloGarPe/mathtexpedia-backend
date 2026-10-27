package mathtexpedia.es.api.presentation.pdfComment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.CommentContentException;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaForbiddenException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaTooManyRequestsException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentErrorDto;
import mathtexpedia.es.api.domain.model.pdfComment.CreateCommentDto;
import mathtexpedia.es.api.domain.model.pdfComment.ReportCommentDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfComment.PdfCommentReportService;
import mathtexpedia.es.api.service.pdfComment.PdfCommentService;
import org.slf4j.event.Level;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comentarios de PDFs", description = "Comentar, responder, borrar y denunciar. Requiere JWT, no requiere rol ADMIN. "
        + "Los comentarios nuevos se revisan antes de publicarse: nacen en PENDING y pasan a PUBLISHED, REJECTED o REVIEW")
@RestController
public class PdfCommentController extends GenericController {

    private final PdfCommentService pdfCommentService;
    private final PdfCommentReportService reportService;

    public PdfCommentController(PdfCommentService pdfCommentService, PdfCommentReportService reportService) {
        this.pdfCommentService = pdfCommentService;
        this.reportService = reportService;
    }

    @Operation(summary = "Comento un PDF o respondo a un comentario",
            description = "Devuelve 202 con el comentario en PENDING: aún no es público, solo lo ve su autor (GET /pdf/{pdfId}/comments/me). "
                    + "Para responder se indica 'replyToId' (una raíz o una respuesta); la respuesta cuelga siempre de la raíz")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Comentario recibido; queda pendiente de revisión"),
            @ApiResponse(responseCode = "400", description = "Texto inválido (cuerpo con 'code': EMPTY, TOO_LONG, LINKS_NOT_ALLOWED, "
                    + "IMAGES_NOT_ALLOWED, HTML_NOT_ALLOWED), o 'replyToId' de otro PDF"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "403", description = "El usuario todavía no ha completado su perfil (nombre de usuario)"),
            @ApiResponse(responseCode = "404", description = "No existe el PDF o el comentario al que se responde"),
            @ApiResponse(responseCode = "409", description = "El comentario al que se responde no está publicado"),
            @ApiResponse(responseCode = "429", description = "El usuario ya tiene el máximo de comentarios pendientes de revisión")
    })
    @PostMapping("/pdf/{pdfId}/comments")
    public ResponseEntity<CommentDto> create(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @RequestBody @Valid CreateCommentDto body,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException,
            MathtexpediaInvalidException, MathtexpediaConflictException, MathtexpediaTooManyRequestsException {
        logger.debug("Called create comment on PDF {} by user {}", pdfId, user.getId());

        return ResponseEntity.accepted().body(pdfCommentService.create(pdfId, body, user));
    }

    @Operation(summary = "Mis comentarios en un PDF",
            description = "Todos los del usuario salvo los borrados, con su estado: PENDING y REVIEW ('en revisión'), PUBLISHED, "
                    + "REJECTED (con 'moderationCategory') u HIDDEN. Pensado para pintar los propios y sondear mientras haya PENDING")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentarios del usuario (puede estar vacía)"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @GetMapping("/pdf/{pdfId}/comments/me")
    public ResponseEntity<List<CommentDto>> getMine(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called getMine on PDF {} by user {}", pdfId, user.getId());

        return ResponseEntity.ok(pdfCommentService.getMine(pdfId, user));
    }

    @Operation(summary = "Borro un comentario",
            description = "Idempotente. Lo puede borrar su autor o un administrador. Si una raíz con respuestas se borra, "
                    + "en el listado público queda como no disponible")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "El comentario está borrado"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "403", description = "No es el autor del comentario ni administrador"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException {
        logger.debug("Called delete comment {} by user {}", commentId, user.getId());

        pdfCommentService.delete(commentId, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Denuncio un comentario",
            description = "Idempotente por usuario. Cuando lo denuncian 3 usuarios distintos el comentario se oculta a la espera "
                    + "de un administrador. Nunca se revela quién denuncia")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Denuncia registrada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (motivo ausente o desconocido)"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o cuenta de usuario inactiva"),
            @ApiResponse(responseCode = "403", description = "Es un comentario del propio usuario"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id"),
            @ApiResponse(responseCode = "409", description = "El comentario no está publicado")
    })
    @PostMapping("/comments/{commentId}/report")
    public ResponseEntity<Void> report(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @RequestBody @Valid ReportCommentDto body,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaForbiddenException, MathtexpediaNotFoundException,
            MathtexpediaConflictException {
        logger.debug("Called report comment {} with {} by user {}", commentId, body.getReason(), user.getId());

        reportService.report(commentId, body.getReason(), user);
        return ResponseEntity.noContent().build();
    }

    /** Este 400 lleva un cuerpo con el motivo; el resto de errores de la API no tienen cuerpo. */
    @ExceptionHandler(CommentContentException.class)
    protected ResponseEntity<CommentErrorDto> handleCommentContentException(CommentContentException e) {
        logErrorAndHttpResponse(e, "400", Level.WARN);

        return ResponseEntity.badRequest().body(new CommentErrorDto(e.getError().name()));
    }
}
