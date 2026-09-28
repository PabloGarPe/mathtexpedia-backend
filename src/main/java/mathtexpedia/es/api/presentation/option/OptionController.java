package mathtexpedia.es.api.presentation.option;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.option.CreateOptionDto;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.option.UpdateOptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.option.OptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Option", description = "Gestión de opciones de preguntas; crear/borrar/actualizar requiere rol ADMIN")
@RestController
@RequestMapping("/option")
public class OptionController extends GenericController {

    private final OptionService optionService;

    public OptionController(OptionService optionService) {
        this.optionService = optionService;
    }

    @Operation(summary = "Obtiene una opción por su ID", description = "Requiere autenticación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opción encontrada"),
            @ApiResponse(responseCode = "404", description = "Opción no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<OptionDto> getOption(
            @Parameter(description = "ID de la opción", required = true)
            @PathVariable long id
    ) {
        logger.debug("Called getOption with id: {}", id);

        return optionService.getOption(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea una nueva opción", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opción creada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto al crear la opción"),
    })
    @PostMapping("/create")
    public ResponseEntity<OptionDto> createOption(
            @Parameter(description = "Datos de la opción a crear", required = true)
            @RequestBody @Valid CreateOptionDto optionDto,
            @Parameter(description = "Perfil del usuario autenticado", required = true)
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called createOption with optionDto: {}", optionDto);

        checkIfAdmin(user);

        OptionDto createdOption = optionService.create(optionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOption);
    }

    @Operation(summary = "Elimina una opción", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Opción eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna opción con el ID proporcionado")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteOption(
            @Parameter(description = "ID de la opción a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called deleteOption with id: {}", id);

        checkIfAdmin(user);

        optionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza una opción por su ID", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opción actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna opción con el ID proporcionado"),
            @ApiResponse(responseCode = "409", description = "Conflicto al actualizar la opción")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<OptionDto> updateOption(
            @Parameter(description = "ID de la opción a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos de la opción a actualizar", required = true)
            @RequestBody @Valid UpdateOptionDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called updateOption with id: {} and dto: {}", id, dto);

        checkIfAdmin(user);

        OptionDto updatedOption = optionService.update(id, dto);
        return ResponseEntity.ok(updatedOption);
    }
}
