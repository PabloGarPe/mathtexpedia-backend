package mathtexpedia.es.api.presentation.pdf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.pdf.UpdatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "PDFs", description = "Gestión de PDFs (consulta requiere estar autenticado; crear/borrar/actualizar requiere rol ADMIN)")
@RestController
@RequestMapping("/pdf")
public class PDFController extends GenericController {

    private final PDFService pdfService;

    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @Operation(summary = "Lista todos los PDFs")
    @GetMapping
    public ResponseEntity<List<PDFDto>> getPDFs() {
        return ResponseEntity.ok(pdfService.getPDFs());
    }

    @Operation(summary = "Obtiene un PDF por su nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF encontrado"),
            @ApiResponse(responseCode = "400", description = "No existe ningún PDF con ese nombre")
    })
    @GetMapping("/pdf/{pdfName}")
    public ResponseEntity<PDFDto> getPDF(
            @Parameter(description = "Nombre del PDF a obtener", required = true)
            @PathVariable String pdfName
    ) {
        logger.debug("Called recieve to get PDF {}", pdfName);

        Optional<PDFDto> pdf = pdfService.getPDF(pdfName);
        return pdf.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un nuevo PDF en el catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna tema con ese id"),
            @ApiResponse(responseCode = "409", description = "Ya existe un PDF con ese nombre")
    })
    @PostMapping("/create")
    public ResponseEntity<PDFDto> createPDF(
            @Parameter(description = "Datos del PDF a crear", required = true)
            @RequestBody @Valid CreatePDFDto pdf,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.debug("Called recieve to create PDF {}", pdf);

        checkIfAdmin(user);

        PDFDto created = pdfService.createPDF(pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @Operation(summary = "Borra un PDF por su nombre", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "PDF borrado"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese nombre")
    })
    @DeleteMapping("/delete/{pdfName}")
    public ResponseEntity<PDFDto> deletePDF(
            @Parameter(description = "Nombre exacto del PDF a borrar", required = true)
            @PathVariable String pdfName,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called recieve to delete PDF {}", pdfName);

        checkIfAdmin(user);

        pdfService.deletePDF(pdfName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @Operation(summary = "Actualiza un PDF existente",
            description = "El body reemplaza los datos del PDF identificado por pdfId. Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "No existe ningún PDF con ese id"),
            @ApiResponse(responseCode = "409", description = "Ya existe un PDF con ese nombre")
    })
    @PatchMapping("/update")
    public ResponseEntity<PDFDto> updatePDF(
            @Parameter(description = "Id del PDF a actualizar", required = true)
            @RequestParam long pdfId,
            @Parameter(description = "Datos del PDF a actualizar", required = true)
            @RequestBody @Valid UpdatePDFDto pdf,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.debug("Called recieve to update PDF {}", pdf);

        checkIfAdmin(user);

        PDFDto updatedPdf = pdfService.updatePDF(pdfId, pdf);
        return ResponseEntity.ok(updatedPdf);
    }

}
