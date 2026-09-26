package mathtexpedia.es.api.presentation.pdf;

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
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFContent;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PDFs", description = "Gestión de PDFs (consulta requiere estar autenticado; crear/borrar/actualizar requiere rol ADMIN)")
@RestController
@RequestMapping("/pdf")
public class PDFController extends GenericController {

    private final PDFService pdfService;

    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @Operation(summary = "Obtiene el contenido del PDF para visualizarlo",
            description = "Devuelve el binario (application/pdf) servido desde el back, pensado para renderizarlo con pdf.js. "
                    + "Nunca se expone la URL de S3.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contenido del PDF"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @GetMapping("/{pdfId}")
    public ResponseEntity<InputStreamResource> getPDF(
            @Parameter(description = "Id del PDF", required = true) @PathVariable long pdfId,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaNotFoundException {
        logger.debug("Called receive to get content of PDF {} by user {}", pdfId, user.getId());

        PDFContent content = pdfService.getPDFContent(pdfId, user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(new InputStreamResource(content.stream()));
    }


    @Operation(summary = "Crea un nuevo PDF en el catálogo",
            description = "multipart/form-data con la parte 'data' (JSON) y la parte 'file' (PDF). Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o fichero no válido"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe la asignatura o el tema indicados"),
            @ApiResponse(responseCode = "409", description = "Ya existe un PDF con ese nombre")
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PDFDto> createPDF(
            @Parameter(description = "Datos del PDF a crear", required = true)
            @RequestPart("data") @Valid CreatePDFDto pdf,
            @Parameter(description = "Fichero PDF", required = true)
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaConflictException,
            MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.debug("Called receive to create PDF {}", pdf);

        checkIfAdmin(user);

        PDFDto created = pdfService.createPDF(pdf, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualiza un PDF existente",
            description = "multipart/form-data con la parte 'data' (JSON) y, opcionalmente, la parte 'file' "
                    + "si se quiere reemplazar el contenido. Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o fichero no válido"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe el PDF, la asignatura o el tema indicados"),
            @ApiResponse(responseCode = "409", description = "Ya existe un PDF con ese nombre")
    })
    @PutMapping(value = "/update/{pdfId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PDFDto> updatePDF(
            @Parameter(description = "Id del PDF a actualizar", required = true)
            @PathVariable long pdfId,
            @Parameter(description = "Datos del PDF a actualizar", required = true)
            @RequestPart("data") @Valid UpdatePDFDto pdf,
            @Parameter(description = "Nuevo fichero PDF (opcional)")
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaConflictException,
            MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.debug("Called receive to update PDF {} with {} (new file: {})", pdfId, pdf, file != null && !file.isEmpty());

        checkIfAdmin(user);

        PDFDto updated = pdfService.updatePDF(pdfId, pdf, file);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Borra un PDF (metadatos y fichero en S3)", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "PDF borrado"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id")
    })
    @DeleteMapping("/delete/{pdfId}")
    public ResponseEntity<Void> deletePDF(
            @Parameter(description = "Id del PDF a borrar", required = true)
            @PathVariable long pdfId,
            @AuthenticationPrincipal UserProfile user)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called receive to delete PDF {}", pdfId);

        checkIfAdmin(user);

        pdfService.deletePDF(pdfId);
        return ResponseEntity.noContent().build();
    }
}