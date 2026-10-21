package mathtexpedia.es.api.presentation.subjectSubscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.CourseSubscriptionDto;
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

    @Operation(summary = "Lista los cursos a los que estoy suscrito",
            description = "Cursos (grado, año y cuatrimestre) a los que me he suscrito con PUT /degree/{degreeId}/year/{year}: las "
                    + "asignaturas que se coloquen después en ellos se me suscriben solas. semester nulo es el año entero. Por grado y "
                    + "año hay o una entrada del año entero o entradas para el cuatrimestre 1 y/o 2. Ordenados por nombre del grado, "
                    + "año y cuatrimestre (el año entero primero). Lista vacía si no hay ninguna")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripciones a curso del usuario (puede estar vacía)"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/courses")
    public ResponseEntity<List<CourseSubscriptionDto>> getMyCourseSubscriptions(
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException {
        logger.debug("Called getMyCourseSubscriptions for user: {}", user);

        return ResponseEntity.ok(subscriptionService.getMyCourseSubscriptions(user));
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

    @Operation(summary = "Me suscribo a un curso entero",
            description = "Suscribe a todas las asignaturas colocadas ahora en ese grado y año (y cuatrimestre, si se indica) y "
                    + "recuerda la suscripción al curso: las asignaturas que se coloquen después en él se suscriben solas (las anuales, "
                    + "solo si la suscripción es al año entero). Suscribirse al año entero sustituye a las suscripciones a sus "
                    + "cuatrimestres. Idempotente: las que ya estaban suscritas se conservan. Devuelve las suscripciones a las "
                    + "asignaturas de ese curso, ordenadas por nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Suscripciones del usuario a las asignaturas del curso (vacía si el curso no tiene asignaturas)"),
            @ApiResponse(responseCode = "400", description = "El año está fuera de 1..años del grado o el cuatrimestre no es 1 ni 2"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID")
    })
    @PutMapping("/degree/{degreeId}/year/{year}")
    public ResponseEntity<List<SubjectSubscriptionDto>> subscribeToCourse(
            @Parameter(description = "ID del grado", required = true)
            @PathVariable long degreeId,
            @Parameter(description = "Año (curso) del grado, de 1 a sus años", required = true)
            @PathVariable int year,
            @Parameter(description = "Cuatrimestre (1 o 2). Si se omite, incluye las asignaturas de cualquier cuatrimestre y las anuales")
            @RequestParam(required = false) Integer semester,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.debug("Called subscribeToCourse degree {} year {} semester {} for user: {}", degreeId, year, semester, user);

        return ResponseEntity.ok(subscriptionService.subscribeToCourse(user, degreeId, year, semester));
    }

    @Operation(summary = "Me doy de baja de un curso entero",
            description = "Da de baja de todas las asignaturas colocadas ahora en ese grado y año (y cuatrimestre, si se indica), "
                    + "también de las que estén colocadas además en otro curso al que el usuario siga suscrito, y olvida la suscripción "
                    + "al curso. Darse de baja del año entero borra también las de sus cuatrimestres; darse de baja de un cuatrimestre "
                    + "estando suscrito al año entero deja la suscripción solo al otro cuatrimestre. "
                    + "Idempotente: devuelve 204 también si no había ninguna suscripción")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "El usuario ya no está suscrito a las asignaturas del curso"),
            @ApiResponse(responseCode = "400", description = "El año está fuera de 1..años del grado o el cuatrimestre no es 1 ni 2"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "No existe ningún grado con ese ID")
    })
    @DeleteMapping("/degree/{degreeId}/year/{year}")
    public ResponseEntity<Void> unsubscribeFromCourse(
            @Parameter(description = "ID del grado", required = true)
            @PathVariable long degreeId,
            @Parameter(description = "Año (curso) del grado, de 1 a sus años", required = true)
            @PathVariable int year,
            @Parameter(description = "Cuatrimestre (1 o 2). Si se omite, incluye las asignaturas de cualquier cuatrimestre y las anuales")
            @RequestParam(required = false) Integer semester,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException {
        logger.debug("Called unsubscribeFromCourse degree {} year {} semester {} for user: {}", degreeId, year, semester, user);

        subscriptionService.unsubscribeFromCourse(user, degreeId, year, semester);
        return ResponseEntity.noContent().build();
    }
}
