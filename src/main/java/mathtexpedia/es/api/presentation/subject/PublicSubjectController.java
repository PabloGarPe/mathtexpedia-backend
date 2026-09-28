package mathtexpedia.es.api.presentation.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.service.pdf.PDFService;
import mathtexpedia.es.api.service.subject.SubjectService;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Asignaturas públicas", description = "Consulta del catálogo de asignaturas y temas sin necesidad de autenticación")
@RestController
@RequestMapping("subject")
public class PublicSubjectController implements PublicEndpoint {

    private final SubjectService subjectService;
    private final SubjectUnitService subjectUnitService;
    private final PDFService pdfService;

    public PublicSubjectController(SubjectService subjectService, SubjectUnitService subjectUnitService, PDFService pdfService) {
        this.subjectService = subjectService;
        this.subjectUnitService = subjectUnitService;
        this.pdfService = pdfService;
    }

    @Operation(summary = "Lista todas las asignaturas", security = { @SecurityRequirement })
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getSubjects() {
        return ResponseEntity.ok(subjectService.getSubjects());
    }

    @Operation(summary = "Obtiene una asignatura por su ID", security = { @SecurityRequirement })
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> getSubject(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) {
        Optional<SubjectDto> subject = subjectService.getSubject(id);
        return subject.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtiene los temas de una asignatura", security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de temas obtenida"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID")
    })
    @GetMapping("/{id}/units")
    public ResponseEntity<List<SubjectUnitDto>> getSubjectUnits(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        List<SubjectUnitDto> subjectUnits = subjectUnitService.getSubjectsUnitsBySubjectId(id);
        return ResponseEntity.ok(subjectUnits);
    }

    @Operation(summary = "Obtiene un tema de una asignatura por su ID", security = { @SecurityRequirement })
    @GetMapping("/unit/{id}")
    public ResponseEntity<SubjectUnitDto> getSubjectUnit(
            @Parameter(description = "ID del tema", required = true)
            @PathVariable long id
    ) {
        Optional<SubjectUnitDto> subjectUnit = subjectUnitService.getSubjectUnit(id);
        return subjectUnit.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtiene todos los pdfs de una asignatura", security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pdfs obtenida"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID")
    })
    @GetMapping("/{id}/pdfs")
    public ResponseEntity<List<PDFDto>> getSubjectPDFs(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        List<PDFDto> pdfs = pdfService.getPDFsBySubject(id);
        return ResponseEntity.ok(pdfs);
    }
}
