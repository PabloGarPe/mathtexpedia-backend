package mathtexpedia.es.api.presentation.userAccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.MathtexpediaConflictException;
import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.userAccount.UpdateUserAccountDto;
import mathtexpedia.es.api.domain.model.userAccount.UserAccountDto;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            description = "Sustituye todos los datos del perfil: el username es obligatorio y los demás campos, opcionales; "
                    + "uno que no se envíe (o venga en blanco) se borra. El primer guardado marca el perfil como completado. "
                    + "No toca la foto de perfil")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (username vacío o con formato incorrecto, campos demasiado largos)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "El username ya lo usa otro usuario (sin distinguir mayúsculas)")
    })
    @PutMapping
    public ResponseEntity<UserAccountDto> updateMe(
            @RequestBody @Valid UpdateUserAccountDto dto,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaConflictException, MathtexpediaUnauthorizedException {
        logger.debug("Called updateMe for user: {} with dto: {}", user, dto);
        return ResponseEntity.ok(userAccountService.updateUserAccount(user, dto));
    }

    @Operation(summary = "Sube o cambia mi foto de perfil",
            description = "multipart/form-data con la parte 'file': imagen JPEG o PNG de hasta 2 MB y al menos 64x64 píxeles. "
                    + "Se recorta a cuadrado, se reduce a 256 px como máximo y se guarda como JPEG; la imagen original no se conserva. "
                    + "Sustituye a la anterior. Devuelve el perfil actualizado (con hasAvatar y avatarUpdatedAt)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto guardada"),
            @ApiResponse(responseCode = "400", description = "Falta el fichero, no es JPEG/PNG, está corrupto, pesa más de 2 MB o no cumple el tamaño mínimo/máximo en píxeles"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "413", description = "La petición supera el límite de subida del servidor")
    })
    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserAccountDto> updateAvatar(
            @Parameter(description = "Imagen JPEG o PNG", required = true)
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException, MathtexpediaInvalidException {
        logger.debug("Called updateAvatar for user: {}", user);
        return ResponseEntity.ok(userAccountService.updateAvatar(user, file));
    }

    @Operation(summary = "Elimina mi foto de perfil",
            description = "Idempotente: si no tenía foto no hace nada. Devuelve el perfil actualizado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto eliminada (o no existía)"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/avatar")
    public ResponseEntity<UserAccountDto> deleteAvatar(
            @AuthenticationPrincipal UserProfile user
    ) throws MathtexpediaUnauthorizedException {
        logger.debug("Called deleteAvatar for user: {}", user);
        return ResponseEntity.ok(userAccountService.deleteAvatar(user));
    }
}
