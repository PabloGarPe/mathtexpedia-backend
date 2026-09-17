package mathtexpedia.es.api.presentation.quiz;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.model.quiz.QuizDto;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.service.quiz.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "Lista todos los cuestionarios", security = { @SecurityRequirement})
    @GetMapping
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getQuizzes());
    }
}
