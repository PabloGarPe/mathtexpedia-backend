package mathtexpedia.es.api.presentation.quizAttempt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.quizAttempt.QuizAttemptDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.quizAttempt.QuizAttemptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Quiz Attempts", description = "Historial de intentos de cuestionarios del usuario autenticado")
@RestController
@RequestMapping("/attempts")
public class QuizAttemptController extends GenericController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        super();
        this.quizAttemptService = quizAttemptService;
    }

    @Operation(summary = "Seleccionar mis intentos de quizzes", description = "Obtiene los intentos de cuestionarios del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de intentos de cuestionarios del usuario autenticado")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @GetMapping
    public Page<QuizAttemptDto> getMyAttempts(
            Pageable pageable,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.trace("Called getMyAttempts with pageable: {} and user: {}", pageable, user);

        return quizAttemptService.getMyAttempts(user, pageable);
    }
}
