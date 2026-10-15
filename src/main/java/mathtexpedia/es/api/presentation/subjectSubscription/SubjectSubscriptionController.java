package mathtexpedia.es.api.presentation.subjectSubscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.SubjectSubscriptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.subjectSubscription.SubjectSubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Suscripciones", description = "Asignaturas a las que está suscrito el usuario autenticado. Requiere JWT, no requiere rol ADMIN")
@RestController
@RequestMapping("/me/subscriptions")
public class SubjectSubscriptionController extends GenericController {

    private final SubjectSubscriptionService subscriptionService;

    public SubjectSubscriptionController(SubjectSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Lista las asignaturas a las que estoy suscrito",
            description = "Ordenadas por nombre de asignatura. Lista vacía si no hay ninguna suscripción")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripciones del usuario (puede estar vacía)"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<List<SubjectSubscriptionDto>> getMySubscriptions(
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException {
        logger.debug("Called getMySubscriptions for user: {}", user);

        return ResponseEntity.ok(subscriptionService.getMySubscriptions(user));
    }

    @Operation(summary = "Me suscribo a una asignatura",
            description = "Idempotente: si ya estaba suscrito devuelve la suscripción existente con 200")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripción activa"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe ninguna asignatura con ese ID")
    })
    @PutMapping("/{subjectId}")
    public ResponseEntity<SubjectSubscriptionDto> subscribe(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long subjectId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException {
        logger.debug("Called subscribe to subject {} for user: {}", subjectId, user);

        return ResponseEntity.ok(subscriptionService.subscribe(user, subjectId));
    }

    @Operation(summary = "Me doy de baja de una asignatura",
            description = "Idempotente: devuelve 204 también si no estaba suscrito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "El usuario ya no está suscrito a la asignatura"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/{subjectId}")
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "ID de la asignatura", required = true)
            @PathVariable long subjectId,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException {
        logger.debug("Called unsubscribe from subject {} for user: {}", subjectId, user);

        subscriptionService.unsubscribe(user, subjectId);
        return ResponseEntity.noContent().build();
    }
}
