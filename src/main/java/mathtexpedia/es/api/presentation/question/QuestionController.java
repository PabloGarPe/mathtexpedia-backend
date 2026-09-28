package mathtexpedia.es.api.presentation.question;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.option.OptionDto;
import mathtexpedia.es.api.domain.model.question.CreateQuestionDto;
import mathtexpedia.es.api.domain.model.question.QuestionDto;
import mathtexpedia.es.api.domain.model.question.UpdateQuestionDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.option.OptionService;
import mathtexpedia.es.api.service.question.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Question", description = "Gestión de preguntas; crear/borrar/actualizar requiere rol ADMIN")
@RestController
@RequestMapping("/question")
public class QuestionController extends GenericController {

    private final QuestionService questionService;
    private final OptionService optionService;

    public QuestionController(QuestionService questionService, OptionService optionService) {
        this.questionService = questionService;
        this.optionService = optionService;
    }

    @Operation(summary = "Obtiene una pregunta por su ID", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pregunta encontrada"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestion(
            @Parameter(description = "ID de la pregunta", required = true)
            @PathVariable long id
    ) {
        logger.debug("Called getQuestion with id: {}", id);

        Optional<QuestionDto> questionDto = questionService.getQuestion(id);
        return questionDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea una nueva pregunta", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pregunta creada"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "No existe el cuestionario a la que se quiere asociar la pregunta"),
            @ApiResponse(responseCode = "409", description = "Conflicto al crear la pregunta")
    })
    @PostMapping("/create")
    public ResponseEntity<QuestionDto> createQuestion(
            @Parameter(description = "Datos de la pregunta a crear", required = true)
            @RequestBody @Valid CreateQuestionDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaNotFoundException {
        logger.debug("Called createQuestion with dto: {}", dto);

        checkIfAdmin(user);

        QuestionDto createdQuestion = questionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @Operation(summary = "Elimina una pregunta por su ID", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pregunta eliminada"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "ID de la pregunta a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called deleteQuestion with id: {}", id);

        checkIfAdmin(user);

        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza una pregunta por su ID", description = "Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pregunta actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto al actualizar la pregunta")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(
            @Parameter(description = "ID de la pregunta a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos de la pregunta a actualizar", required = true)
            @RequestBody @Valid UpdateQuestionDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called updateQuestion with id: {} and dto: {}", id, dto);

        checkIfAdmin(user);

        QuestionDto updatedQuestion = questionService.update(id, dto);
        return ResponseEntity.ok(updatedQuestion);
    }

    @Operation(summary = "Obtiene una pregunta con sus opciones por su ID", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pregunta encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Pregunta no encontrada")
    })
    @GetMapping("/{id}/options")
    public ResponseEntity<List<OptionDto>> getQuestionOptions(
            @Parameter(description = "ID de la pregunta", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getQuestionOptions with id: {}", id);

        List<OptionDto> questionDto = optionService.getOptionsByQuestion(id);
        return ResponseEntity.ok(questionDto);
    }
}