package mathtexpedia.es.api.presentation.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subject.CreateSubjectDto;
import mathtexpedia.es.api.domain.model.subject.SubjectDto;
import mathtexpedia.es.api.domain.model.subject.UpdateSubjectDto;
import mathtexpedia.es.api.domain.model.subjectUnit.CreateSubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.SubjectUnitDto;
import mathtexpedia.es.api.domain.model.subjectUnit.UpdateSubjectUnitDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.subject.SubjectService;
import mathtexpedia.es.api.service.subjectUnit.SubjectUnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Subjects", description = "Gestión de asignaturas y temas (consulta no requiere autenticación; crear/borrar/actualizar requiere rol ADMIN)")
@RestController
@RequestMapping("/subject")
public class SubjectController extends GenericController {

    private final SubjectService subjectService;
    private final SubjectUnitService subjectUnitService;

    public SubjectController(SubjectService subjectService, SubjectUnitService subjectUnitService) {
        this.subjectService = subjectService;
        this.subjectUnitService = subjectUnitService;
    }

    @Operation(summary = "Lista todas las asignaturas")
    @GetMapping
    public ResponseEntity<List<SubjectDto>> getSubjects() {
        return ResponseEntity.ok(subjectService.getSubjects());
    }

    @Operation(summary = "Obtiene una asignatura por su ID", description = "Consulta pública, no requiere autenticación")
    @GetMapping("/{id}")
    public ResponseEntity<SubjectDto> getSubject(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) {
        logger.debug("Called getSubject with id {}", id);

        Optional<SubjectDto> subject = subjectService.getSubject(id);
        return subject.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea una nueva asignatura en el catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asignatura creada"),
            @ApiResponse(responseCode = "409", description = "Ya existe una asignatura con ese nombre"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @PostMapping("/create")
    public ResponseEntity<SubjectDto> createSubject(
            @Parameter(description = "Datos de la asignatura a crear", required = true)
            @RequestBody @Valid CreateSubjectDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException {
        logger.debug("Called createSubject with dto {}", dto);

        checkIfAdmin(user);

        SubjectDto createdSubject = subjectService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
    }

    @Operation(summary = "Elimina una asignatura del catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Asignatura eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar la asignatura porque tiene temas asociados"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSubject(
            @Parameter(description = "ID de la asignatura a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.debug("Called deleteSubject with id {}", id);

        checkIfAdmin(user);

        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza una asignatura del catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asignatura actualizada"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe una asignatura con ese nombre")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<SubjectDto> updateSubject(
            @Parameter(description = "ID de la asignatura a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos para actualizar la asignatura", required = true)
            @RequestBody @Valid UpdateSubjectDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called updateSubject with id {} and dto {}", id, dto);

        checkIfAdmin(user);

        SubjectDto updatedSubject = subjectService.update(id, dto);
        return ResponseEntity.ok(updatedSubject);
    }

    @Operation(summary = "Obtiene los temas de una asignatura", description = "Consulta pública, no requiere autenticación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de temas obtenida"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID")
    })
    @GetMapping("/{id}/units")
    public ResponseEntity<List<SubjectUnitDto>> getSubjectUnits(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getSubjectUnits with id {}", id);

        List<SubjectUnitDto> subjectUnits = subjectUnitService.getSubjectsUnitsBySubjectId(id);
        return ResponseEntity.ok(subjectUnits);
    }

    @Operation(summary = "Obtiene un tema de una asignatura por su ID", description = "Consulta pública, no requiere autenticación")
    @GetMapping("/unit/{id}")
    public ResponseEntity<SubjectUnitDto> getSubjectUnit(
            @Parameter(description = "ID del tema", required = true)
            @PathVariable long id
    ) {
        logger.debug("Called getSubjectUnit with id {}", id);

        Optional<SubjectUnitDto> subjectUnit = subjectUnitService.getSubjectUnit(id);
        return subjectUnit.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un nuevo tema para una asignatura", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tema creado"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tema con ese nombre para la asignatura"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @PostMapping("/{subjectId}/unit/create")
    public ResponseEntity<SubjectUnitDto> createSubjectUnit(
            @Parameter(description = "ID de la asignatura a la que pertenece el tema", required = true)
            @PathVariable long subjectId,
            @Parameter(description = "Datos del tema a crear", required = true)
            @RequestBody @Valid CreateSubjectUnitDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called createSubjectUnit with subjectId {} and dto {}", subjectId, dto);

        checkIfAdmin(user);

        SubjectUnitDto createdSubjectUnit = subjectUnitService.create(dto, subjectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubjectUnit);
    }

    @Operation(summary = "Elimina un tema de una asignatura", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tema eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún tema con ese ID"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar el tema porque tiene PDFs asociados")
    })
    @DeleteMapping("/unit/delete/{id}")
    public ResponseEntity<Void> deleteSubjectUnit(
            @Parameter(description = "ID del tema a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called deleteSubjectUnit with id {}", id);

        checkIfAdmin(user);

        subjectUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza un tema de una asignatura", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tema actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún tema con ese ID"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe un tema con ese nombre para la asignatura")
    })
    @PutMapping("/unit/update/{id}")
    public ResponseEntity<SubjectUnitDto> updateSubjectUnit(
            @Parameter(description = "ID del tema a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos para actualizar el tema", required = true)
            @RequestBody @Valid UpdateSubjectUnitDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called updateSubjectUnit with id {} and dto {}", id, dto);

        checkIfAdmin(user);

        SubjectUnitDto updatedSubjectUnit = subjectUnitService.update(id, dto);
        return ResponseEntity.ok(updatedSubjectUnit);
    }
}
