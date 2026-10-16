package mathtexpedia.es.api.presentation.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.pdf.PDFDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDetailDto;
import mathtexpedia.es.api.domain.model.subject.SubjectFilter;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.pdf.PDFService;
import mathtexpedia.es.api.service.subject.SubjectService;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Asignaturas públicas", description = "Consulta del catálogo de asignaturas y temas sin necesidad de autenticación")
@RestController
@RequestMapping("subject")
public class PublicSubjectController extends GenericController implements PublicEndpoint {

    private final SubjectService subjectService;
    private final SubjectUnitService subjectUnitService;
    private final PDFService pdfService;

    public PublicSubjectController(SubjectService subjectService, SubjectUnitService subjectUnitService, PDFService pdfService) {
        this.subjectService = subjectService;
        this.subjectUnitService = subjectUnitService;
        this.pdfService = pdfService;
    }

    @Operation(summary = "Lista las asignaturas con sus colocaciones en grados",
            description = "Ordenadas por nombre. Sin filtros devuelve todas. 'year' y 'semester' exigen 'degreeId' (también 'semester' sin 'year'). "
                    + "'unassigned=true' lista las asignaturas que no están colocadas en ningún grado y no se puede combinar con 'degreeId'",
            security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de asignaturas obtenida (puede estar vacía)"),
            @ApiResponse(responseCode = "400", description = "Combinación de filtros inválida o 'year'/'semester' fuera de rango"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese 'degreeId'")
    })
    @GetMapping
    public ResponseEntity<List<SubjectDetailDto>> getSubjects(
            @Parameter(description = "Solo las asignaturas colocadas en este grado")
            @RequestParam(required = false) Long degreeId,
            @Parameter(description = "Solo las colocadas en este año del grado (requiere degreeId)")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Solo las colocadas en este cuatrimestre, 1 o 2 (requiere degreeId)")
            @RequestParam(required = false) Integer semester,
            @Parameter(description = "Solo las asignaturas sin colocación en ningún grado (no combinable con degreeId)")
            @RequestParam(defaultValue = "false") boolean unassigned
    ) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        return ResponseEntity.ok(subjectService.getSubjects(new SubjectFilter(degreeId, year, semester, unassigned)));
    }

    @Operation(summary = "Obtiene una asignatura por su ID, con sus colocaciones en grados", security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignatura obtenida"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDetailDto> getSubject(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) {
        Optional<SubjectDetailDto> subject = subjectService.getSubject(id);
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
