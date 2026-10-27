package mathtexpedia.es.api.presentation.pdfComment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.model.pdfComment.CommentConfigDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdfComment.PdfCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comentarios públicos", description = "Lectura de los comentarios publicados de un PDF, sin autenticación. "
        + "Los comentarios tienen dos niveles: raíces y sus respuestas")
@RestController
@RequestMapping("comments")
public class PublicCommentConfigController extends GenericController implements PublicEndpoint {

    private final PdfCommentService pdfCommentService;

    public PublicCommentConfigController(PdfCommentService pdfCommentService) {
        this.pdfCommentService = pdfCommentService;
    }

    @Operation(summary = "Límites de los comentarios",
            description = "Longitud máxima de un comentario y comentarios pendientes de revisión permitidos por usuario. "
                    + "Son configurables en el servidor, así que el front debe leerlos de aquí en vez de fijarlos",
            security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Límites vigentes")
    })
    @GetMapping("config")
    public ResponseEntity<CommentConfigDto> getConfig() {
        logger.debug("Called getConfig");

        return ResponseEntity.ok(pdfCommentService.getConfig());
    }
}
