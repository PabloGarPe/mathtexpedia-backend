package mathtexpedia.es.api.presentation.pdf;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.pdf.CreatePDFDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.persistence.pdf.PDF;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PDFs", description = "Gestión de PDFs (consulta requiere estar autenticado; crear/borrar/actualizar requiere rol ADMIN)")
@RestController
@RequestMapping("/pdf")
public class PDFController extends GenericController {

    @Autowired
    private PDFService pdfService;

    @Operation(summary = "Lista todos los PDFs")
    @GetMapping
    public List<PDF> getPDFs() {
        return pdfService.getPDFs();
    }

    @Operation(summary = "Obtiene un PDF por su nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF encontrado"),
            @ApiResponse(responseCode = "400", description = "No existe ningún PDF con ese nombre")
    })
    @GetMapping("/pdf")
    public PDF getPDF(
            @Parameter(description = "Nombre del PDF a obtener", required = true)
            @RequestParam String pdfName) throws MathtexpediaInvalidException {
        return pdfService.getPDF(pdfName);
    }

    @Operation(summary = "Crea un nuevo PDF en el catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "PDF creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ya existe un PDF con ese nombre"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @PostMapping("/create")
    public ResponseEntity<PDF> createPDF(@RequestBody CreatePDFDto pdf,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to create PDF {}", pdf);

        checkIfAdmin(user);

        PDF created = pdfService.createPDF(pdf);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @Operation(summary = "Borra un PDF por su nombre", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "PDF borrado"),
            @ApiResponse(responseCode = "400", description = "No existe ningún PDF con ese nombre"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<PDF> deletePDF(
            @Parameter(description = "Nombre exacto del PDF a borrar", required = true)
            @RequestParam String pdfName,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to delete PDF {}", pdfName);

        checkIfAdmin(user);

        pdfService.deletePDF(pdfName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @Operation(summary = "Actualiza un PDF existente",
            description = "El body reemplaza los datos del PDF identificado por pdfId. Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "PDF actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o no existe un PDF con ese id"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @PatchMapping("/update")
    public ResponseEntity<PDF> updatePDF(@RequestBody PDF pdf,
            @Parameter(description = "Id del PDF a actualizar", required = true)
            @RequestParam long pdfId,
                                         @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException, MathtexpediaUnauthorizedException {
        logger.debug("Called recieve to update PDF {}", pdf);

        checkIfAdmin(user);

        pdf = pdfService.updatePDF(pdf, pdfId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pdf);
    }

}
