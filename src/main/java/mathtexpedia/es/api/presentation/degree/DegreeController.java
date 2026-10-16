package mathtexpedia.es.api.presentation.degree;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.degree.CreateDegreeDto;
import mathtexpedia.es.api.domain.model.degree.DegreeDto;
import mathtexpedia.es.api.domain.model.degree.UpdateDegreeDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.degree.DegreeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Degrees", description = "Gestión de grados (crear/borrar/actualizar requiere rol ADMIN)")
@RestController
@RequestMapping("/degree")
public class DegreeController extends GenericController {

    private final DegreeService degreeService;

    public DegreeController(DegreeService degreeService) {
        this.degreeService = degreeService;
    }

    @Operation(summary = "Crea un nuevo grado en el catálogo", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grado creado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un grado con ese nombre y universidad"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @PostMapping("/create")
    public ResponseEntity<DegreeDto> createDegree(
            @Parameter(description = "Datos del grado a crear", required = true)
            @RequestBody @Valid CreateDegreeDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException {
        logger.debug("Called createDegree with dto {}", dto);

        checkIfAdmin(user);

        DegreeDto createdDegree = degreeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDegree);
    }

    @Operation(summary = "Actualiza un grado del catálogo",
            description = "Requiere rol ADMIN. Reemplaza todos los campos. Si reduce los años, los usuarios con un curso mayor en su perfil lo pierden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grado actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Ya existe un grado con ese nombre y universidad, o hay asignaturas colocadas en los años que desaparecerían")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<DegreeDto> updateDegree(
            @Parameter(description = "ID del grado a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos para actualizar el grado", required = true)
            @RequestBody @Valid UpdateDegreeDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called updateDegree with id {} and dto {}", id, dto);

        checkIfAdmin(user);

        DegreeDto updatedDegree = degreeService.update(id, dto);
        return ResponseEntity.ok(updatedDegree);
    }

    @Operation(summary = "Elimina un grado del catálogo",
            description = "Requiere rol ADMIN. Los usuarios que lo tenían en su perfil se quedan sin grado ni curso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grado eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar el grado porque tiene asignaturas colocadas"),
            @ApiResponse(responseCode = "401", description = "El usuario autenticado no tiene rol ADMIN")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDegree(
            @Parameter(description = "ID del grado a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.debug("Called deleteDegree with id {}", id);

        checkIfAdmin(user);

        degreeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
