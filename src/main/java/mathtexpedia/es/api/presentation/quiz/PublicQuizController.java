package mathtexpedia.es.api.presentation.quiz;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.model.quiz.QuizDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.service.quiz.QuizService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cuestionarios públicos", description = "Consulta de cuestionarios sin necesidad de autenticación")
@RestController
@RequestMapping("quiz")
public class PublicQuizController implements PublicEndpoint {

    private final QuizService quizService;

    public PublicQuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @Operation(summary = "Lista todos los cuestionarios",
            description = "Incluye el número de intentos (attemptCount) de cada cuestionario",
            security = { @SecurityRequirement})
    @GetMapping
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getQuizzes());
    }

    @Operation(summary = "Cuestionarios nuevos o actualizados recientemente",
            description = "Del más reciente al más antiguo según la fecha de última edición. Pensado para la hilera 'Novedades'",
            security = { @SecurityRequirement})
    @GetMapping("recent")
    public ResponseEntity<List<QuizDto>> getRecentQuizzes(
            @Parameter(description = "Máximo de cuestionarios a devolver (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(quizService.getRecentQuizzes(limit));
    }

    @Operation(summary = "Cuestionarios más hechos en general",
            description = "Ordenados de más a menos intentos contando los envíos de todos los usuarios. "
                    + "Solo incluye cuestionarios con al menos un intento. Pensado para la hilera 'Los más hechos'",
            security = { @SecurityRequirement})
    @GetMapping("top")
    public ResponseEntity<List<QuizDto>> getMostAttemptedQuizzes(
            @Parameter(description = "Máximo de cuestionarios a devolver (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(quizService.getMostAttemptedQuizzes(limit));
    }
}
