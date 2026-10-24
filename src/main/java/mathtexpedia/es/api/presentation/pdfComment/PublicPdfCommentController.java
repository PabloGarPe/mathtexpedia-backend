package mathtexpedia.es.api.presentation.pdfComment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentPageDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfComment.PdfCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comentarios públicos", description = "Lectura de los comentarios publicados de un PDF, sin autenticación. "
        + "Los comentarios tienen dos niveles: raíces y sus respuestas")
@RestController
@RequestMapping("pdf")
public class PublicPdfCommentController extends GenericController implements PublicEndpoint {

    private final PdfCommentService pdfCommentService;

    public PublicPdfCommentController(PdfCommentService pdfCommentService) {
        this.pdfCommentService = pdfCommentService;
    }

    @Operation(summary = "Comentarios raíz de un PDF",
            description = "Del más reciente al más antiguo por fecha de publicación. Cada raíz incluye 'replyCount'. Una raíz borrada, "
                    + "oculta o rechazada que conserva respuestas publicadas se devuelve con 'available' a false y sin texto ni autor",
            security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de comentarios (puede estar vacía)"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @GetMapping("{pdfId}/comments")
    public ResponseEntity<CommentPageDto> getRoots(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @Parameter(description = "Número de página, desde 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto 20, máximo 50)") @RequestParam(defaultValue = "20") int size
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getRoots for PDF {}", pdfId);

        return ResponseEntity.ok(pdfCommentService.getRoots(pdfId, page, size));
    }

    @Operation(summary = "Respuestas de un comentario raíz",
            description = "Solo las publicadas, de la más antigua a la más reciente. Cada una indica 'replyToId' y 'replyToUsername' "
                    + "para pintar 'en respuesta a @usuario'",
            security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de respuestas (puede estar vacía)"),
            @ApiResponse(responseCode = "404", description = "No existe ningún comentario raíz con ese id")
    })
    @GetMapping("comments/{commentId}/replies")
    public ResponseEntity<CommentPageDto> getReplies(
            @Parameter(description = "Id del comentario raíz", required = true) @PathVariable long commentId,
            @Parameter(description = "Número de página, desde 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (por defecto 20, máximo 50)") @RequestParam(defaultValue = "20") int size
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getReplies for comment {}", commentId);

        return ResponseEntity.ok(pdfCommentService.getReplies(commentId, page, size));
    }
}
