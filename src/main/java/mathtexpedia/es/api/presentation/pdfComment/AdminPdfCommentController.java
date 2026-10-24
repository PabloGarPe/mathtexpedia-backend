package mathtexpedia.es.api.presentation.pdfComment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdfComment.AdminCommentPageDto;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.RejectCommentDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfComment.PdfCommentAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;

@Tag(name = "Moderación de comentarios", description = "Cola de revisión de comentarios. Requiere JWT con rol ADMIN. "
        + "Para borrar se usa DELETE /comments/{commentId}")
@RestController
@RequestMapping("/admin/comments")
public class AdminPdfCommentController extends GenericController {

    private final PdfCommentAdminService adminService;

    public AdminPdfCommentController(PdfCommentAdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Cola de moderación",
            description = "Comentarios en los estados pedidos, del más antiguo al más reciente, con sus denuncias abiertas. "
                    + "Por defecto REVIEW (dudosos para la IA) y HIDDEN (ocultos por denuncias). También se pueden pedir PENDING "
                    + "(esperando a la IA) y REJECTED (para restaurar falsos positivos)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de comentarios (puede estar vacía)"),
            @ApiResponse(responseCode = "400", description = "Algún estado no es consultable"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o el usuario no es administrador")
    })
    @GetMapping
    public ResponseEntity<AdminCommentPageDto> getQueue(
            @Parameter(description = "Estados separados por comas: PENDING, REVIEW, HIDDEN, REJECTED", example = "REVIEW,HIDDEN")
            @RequestParam(required = false) Set<CommentStatus> status,
            @Parameter(description = "Número de página, desde 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto 20, máximo 50)") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        checkIfAdmin(user);
        logger.debug("Called getQueue by admin {}", user.getId());

        Set<CommentStatus> statuses = status == null || status.isEmpty()
                ? EnumSet.of(CommentStatus.REVIEW, CommentStatus.HIDDEN) : status;
        return ResponseEntity.ok(adminService.getQueue(statuses, page, size));
    }

    @Operation(summary = "Apruebo un comentario",
            description = "Lo publica (también restaura uno oculto o un rechazo erróneo) y cierra sus denuncias. "
                    + "Solo avisa de 'comentario nuevo' si es la primera vez que se publica")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentario publicado"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o el usuario no es administrador"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id"),
            @ApiResponse(responseCode = "409", description = "El comentario ya está publicado o borrado")
    })
    @PostMapping("/{commentId}/approve")
    public ResponseEntity<Void> approve(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        checkIfAdmin(user);
        logger.debug("Called approve comment {} by admin {}", commentId, user.getId());

        adminService.approve(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rechazo un comentario",
            description = "Lo rechaza con una categoría, cierra sus denuncias y avisa a su autor con un motivo genérico")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentario rechazado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (categoría ausente o desconocida)"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o el usuario no es administrador"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario con ese id"),
            @ApiResponse(responseCode = "409", description = "El comentario ya está rechazado o borrado")
    })
    @PostMapping("/{commentId}/reject")
    public ResponseEntity<Void> reject(
            @Parameter(description = "Id del comentario", required = true) @PathVariable long commentId,
            @RequestBody @Valid RejectCommentDto body,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        checkIfAdmin(user);
        logger.debug("Called reject comment {} with {} by admin {}", commentId, body.getCategory(), user.getId());

        adminService.reject(commentId, body.getCategory());
        return ResponseEntity.noContent().build();
    }
}
