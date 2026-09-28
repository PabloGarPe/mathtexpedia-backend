package mathtexpedia.es.api.presentation.userAccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Perfil", description = "Perfil de usuario autenticado. Requiere JWT, no requiere rol ADMIN")
@RestController
@RequestMapping("/me")
public class UserAccountController extends GenericController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Operation(summary = "Perfil del usuario autenticado", description = "Si es el primer acceso del usuario, crea su perfil a partir del JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil del usuario obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public ResponseEntity<UserAccountDto> getMe(
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException {
        logger.debug("Called getMe for user: {}", user);

        return ResponseEntity.ok(userAccountService.getUserAccount(user));
    }

    @Operation(summary = "Actualiza el perfil del usuario autenticado",
            description = "La primera actualización marca el perfil como completado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PutMapping
    public ResponseEntity<UserAccountDto> updateMe(
            @RequestBody @Valid UpdateUserAccountDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException {
        logger.debug("Called updateMe for user: {} with dto: {}", user, dto);
        return ResponseEntity.ok(userAccountService.updateUserAccount(user, dto));
    }
}
