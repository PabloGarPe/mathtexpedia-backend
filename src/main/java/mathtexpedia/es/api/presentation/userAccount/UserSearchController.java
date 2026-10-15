package mathtexpedia.es.api.presentation.userAccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.AuthorCandidateDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Usuarios", description = "Búsqueda de usuarios. Requiere rol ADMIN")
@RestController
@RequestMapping("/users")
public class UserSearchController extends GenericController {

    private final UserAccountService userAccountService;

    public UserSearchController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Operation(summary = "Busca usuarios que pueden ser autores o coautores de un PDF",
            description = "Devuelve los usuarios activos cuyo nombre o email contiene el texto buscado (sin distinguir mayúsculas ni tildes), "
                    + "ordenados por nombre (o por email si no tienen nombre). Pensado para un buscador con sugerencias: "
                    + "el front debe aplicar debounce y cancelar la petición anterior. Si 'q' tiene menos de 2 caracteres "
                    + "(tras quitar espacios) devuelve una lista vacía. Requiere rol ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de candidatos (puede estar vacía)"),
            @ApiResponse(responseCode = "401", description = "Sin token válido o el usuario autenticado no tiene rol ADMIN")
    })
    @GetMapping("/search")
    public ResponseEntity<List<AuthorCandidateDto>> searchUsers(
            @Parameter(description = "Texto a buscar en nombre o email (mínimo 2 caracteres)", example = "ada")
            @RequestParam(required = false) String q,
            @Parameter(description = "Máximo de resultados (por defecto 10, máximo 20)")
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.debug("Called searchUsers by user {} with limit {}", user, limit);

        checkIfAdmin(user);

        return ResponseEntity.ok(userAccountService.searchAuthorCandidates(q, limit));
    }
}
