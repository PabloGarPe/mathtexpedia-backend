package mathtexpedia.es.api.presentation.degree;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.model.degree.CurriculumDto;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.degree.DegreeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Grados públicos", description = "Consulta del catálogo de grados y de sus planes de estudios sin necesidad de autenticación")
@RestController
@RequestMapping("degree")
public class PublicDegreeController extends GenericController implements PublicEndpoint {

    private final DegreeService degreeService;

    public PublicDegreeController(DegreeService degreeService) {
        this.degreeService = degreeService;
    }

    @Operation(summary = "Lista todos los grados",
            description = "Ordenados por nombre y universidad",
            security = { @SecurityRequirement })
    @GetMapping
    public ResponseEntity<List<DegreeDto>> getDegrees() {
        return ResponseEntity.ok(degreeService.getDegrees());
    }

    @Operation(summary = "Obtiene un grado por su ID", security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grado obtenido"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DegreeDto> getDegree(
            @Parameter(description = "ID del grado", required = true)
            @PathVariable long id
    ) {
        Optional<DegreeDto> degree = degreeService.getDegree(id);
        return degree.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Obtiene el plan de estudios de un grado",
            description = "Asignaturas colocadas en el grado agrupadas por año, ordenadas por cuatrimestre (las anuales al final) y nombre. "
                    + "Sin 'year' devuelve todos los años del grado, también los vacíos; con 'year', solo ese",
            security = { @SecurityRequirement })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan de estudios obtenido"),
            @ApiResponse(responseCode = "400", description = "'year' fuera de 1..años del grado o 'semester' distinto de 1 y 2"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID")
    })
    @GetMapping("/{id}/curriculum")
    public ResponseEntity<CurriculumDto> getCurriculum(
            @Parameter(description = "ID del grado", required = true)
            @PathVariable long id,
            @Parameter(description = "Solo este año del grado")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Solo este cuatrimestre, 1 o 2")
            @RequestParam(required = false) Integer semester
    ) throws MathtexpediaNotFoundException, MathtexpediaInvalidException {
        return ResponseEntity.ok(degreeService.getCurriculum(id, year, semester));
    }
}
