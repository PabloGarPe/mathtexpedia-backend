package mathtexpedia.es.api.presentation.quiz;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.question.QuestionDto;
import mathtexpedia.es.api.domain.model.quiz.CreateQuizDto;
import mathtexpedia.es.api.domain.model.quiz.QuizDto;
import mathtexpedia.es.api.domain.model.quiz.QuizExportableDto;
import mathtexpedia.es.api.domain.model.quiz.QuizForAttemptDto;
import mathtexpedia.es.api.domain.model.quiz.UpdateQuizDto;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptResultDto;
import mathtexpedia.es.api.domain.model.quizAttempt.SubmitQuizAttemptDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.question.QuestionService;
import mathtexpedia.es.api.service.quiz.QuizService;
import mathtexpedia.es.api.service.quizAttempt.QuizAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Quiz", description = "Gestión de cuestionarios y preguntas; crear/borrar/actualizar requiere rol ADMIN")
@RestController
@RequestMapping("/quiz")
public class QuizController extends GenericController {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService quizAttemptService;

    public QuizController(QuizService quizService, QuestionService questionService, QuizAttemptService quizAttemptService) {
        this.quizService = quizService;
        this.questionService = questionService;
        this.quizAttemptService = quizAttemptService;
    }

    @Operation(summary = "Obtiene un cuestionario por su ID", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<QuizDto> getQuiz(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long id
    ) {
        logger.debug("Called getQuiz with id: {}", id);

        Optional<QuizDto> quizDto = quizService.getQuiz(id);
        return quizDto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crea un nuevo cuestionario", description = "Requiere rol ADMIN")
    @ApiResponse(responseCode = "201", description = "Cuestionario creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura o unidad de asignatura con el ID proporcionado")
    @ApiResponse(responseCode = "409", description = "Conflicto al crear el cuestionario")
    @PostMapping("/create")
    public ResponseEntity<QuizDto> createQuiz(
            @Parameter(description="Datos del cuestionario a crear", required = true)
            @RequestBody @Valid CreateQuizDto dto,
            @AuthenticationPrincipal UserProfile user
            ) throws MathtexpediaConflictException, MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called createQuiz with dto: {}", dto);

        checkIfAdmin(user);

        QuizDto createdQuiz = quizService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @Operation(summary = "Elimina un cuestionario y sus preguntas asociadas por su ID", description = "Requiere rol ADMIN")
    @ApiResponse(responseCode = "204", description = "Cuestionario eliminado correctamente")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteQuiz(
            @Parameter(description = "ID del cuestionario a eliminar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called deleteQuiz with id: {}", id);

        checkIfAdmin(user);

        quizService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Actualiza un cuestionario por su ID", description = "Requiere rol ADMIN")
    @ApiResponse(responseCode = "200", description = "Cuestionario actualizado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @ApiResponse(responseCode = "409", description = "Conflicto al actualizar el cuestionario")
    @PutMapping("/update/{id}")
    public ResponseEntity<QuizDto> updateQuiz(
            @Parameter(description = "ID del cuestionario a actualizar", required = true)
            @PathVariable long id,
            @Parameter(description = "Datos del cuestionario a actualizar", required = true)
            @RequestBody @Valid UpdateQuizDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaConflictException, MathtexpediaInvalidException, MathtexpediaNotFoundException {
        logger.debug("Called updateQuiz with id: {} and dto: {}", id, dto);

        checkIfAdmin(user);

        QuizDto updatedQuiz = quizService.update(id, dto);
        return ResponseEntity.ok(updatedQuiz);
    }

    @Operation(summary = "Obtiene las preguntas de un cuestionario por su ID", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @ApiResponse(responseCode = "200", description = "Preguntas obtenidas correctamente")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @GetMapping("/{id}/questions")
    public ResponseEntity<List<QuestionDto>> getQuizQuestions(
            @Parameter(description = "ID del cuestionario", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getQuizQuestions with id: {}", id);

        List<QuestionDto> questions = questionService.getQuestionsByQuiz(id);
        return ResponseEntity.ok(questions);
    }

    @Operation(summary = "Obtiene un cuestionario por su ID para intentar resolverlo", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @ApiResponse(responseCode = "200", description = "Cuestionario obtenido correctamente")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @GetMapping("/{id}/attempt")
    public ResponseEntity<QuizForAttemptDto> getQuizForAttempt(
            @Parameter(description = "ID del cuestionario", required = true)
            @PathVariable long id
    ) throws MathtexpediaNotFoundException {
        logger.debug("Called getQuizForAttempt with id: {}", id);

        QuizForAttemptDto quizForAttempt = quizService.getQuizForAttempt(id);
        return ResponseEntity.ok(quizForAttempt);
    }

    @Operation(summary = "Exporta un cuestionario completo a JSON", description = "Requiere rol ADMIN")
    @ApiResponse(responseCode = "200", description = "Cuestionario exportado correctamente")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @GetMapping("/{id}/export")
    public ResponseEntity<QuizExportableDto> exportQuiz(
            @Parameter(description = "ID del cuestionario a exportar", required = true)
            @PathVariable long id,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called exportQuiz with id: {}", id);

        checkIfAdmin(user);

        QuizExportableDto exportedQuiz = quizService.exportQuiz(id);
        return ResponseEntity.ok(exportedQuiz);
    }

    @Operation(summary = "Importa un cuestionario completo desde JSON",
            description = "El JSON no incluye asignatura ni tema; se seleccionan aparte al importar. Requiere rol ADMIN")
    @ApiResponse(responseCode = "201", description = "Cuestionario importado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura o unidad de asignatura con el ID proporcionado")
    @ApiResponse(responseCode = "409", description = "Conflicto al crear el cuestionario")
    @PostMapping("/import")
    public ResponseEntity<QuizDto> importQuiz(
            @Parameter(description = "Cuestionario a importar", required = true)
            @RequestBody @Valid QuizExportableDto dto,
            @Parameter(description = "ID de la asignatura a la que se asocia el cuestionario importado", required = true)
            @RequestParam long subjectId,
            @Parameter(description = "ID del tema al que se asocia el cuestionario importado")
            @RequestParam(required = false) Long subjectUnitId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException, MathtexpediaNotFoundException, MathtexpediaConflictException {
        logger.debug("Called importQuiz with dto: {}, subjectId: {} and subjectUnitId: {}", dto, subjectId, subjectUnitId);

        checkIfAdmin(user);

        QuizDto importedQuiz = quizService.importQuiz(dto, subjectId, subjectUnitId);
        return ResponseEntity.status(HttpStatus.CREATED).body(importedQuiz);
    }

    @Operation(summary = "Envía un intento de cuestionario para su evaluación", description = "Requiere autenticación, pero no requiere rol ADMIN")
    @ApiResponse(responseCode = "200", description = "Intento de cuestionario evaluado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "404", description = "No existe ningún cuestionario con el ID proporcionado")
    @PostMapping("/{id}/submit")
    public QuizAttemptResultDto submitAttempt(
            @Parameter(description = "ID del cuestionario a enviar", required = true)
            @PathVariable long id,
            @Parameter(description = "Intento de cuestionario a enviar", required = true)
            @RequestBody @Valid SubmitQuizAttemptDto dto,
            @AuthenticationPrincipal UserProfile user
            ) throws MathtexpediaNotFoundException, MathtexpediaUnauthorizedException {
        logger.debug("Called submitAttempt with id: {} and dto: {}", id, dto);

        return quizAttemptService.submitAttempt(id, dto, user);
    }
}
