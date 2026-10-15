package mathtexpedia.es.api.presentation.userAccount;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.userAccount.AvatarImageProcessor;
import mathtexpedia.es.api.service.userAccount.UserAccountService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@Tag(name = "Perfil", description = "Fotos de perfil de los usuarios. Requiere JWT, no requiere rol ADMIN")
@RestController
@RequestMapping("/users")
public class UserAvatarController extends GenericController {

    private final UserAccountService userAccountService;

    public UserAvatarController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Operation(summary = "Foto de perfil de un usuario",
            description = "Imagen JPEG cuadrada de hasta 256 px. Solo para usuarios autenticados: al llevar el token en cabecera no sirve "
                    + "como 'src' directo de una etiqueta img, hay que pedirla con fetch y mostrarla como blob. Admite If-None-Match "
                    + "(ETag derivado de avatarUpdatedAt): responde 304 sin cuerpo si no ha cambiado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen JPEG", content = @Content(mediaType = "image/jpeg")),
            @ApiResponse(responseCode = "304", description = "La foto no ha cambiado desde la versión indicada en If-None-Match"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "El usuario no existe, está inactivo o no tiene foto")
    })
    @GetMapping(value = "/{userId}/avatar", produces = AvatarImageProcessor.OUTPUT_CONTENT_TYPE)
    public ResponseEntity<byte[]> getAvatar(
            @Parameter(description = "Identificador interno del usuario", example = "42")
            @PathVariable long userId,
            WebRequest request) throws MathtexpediaNotFoundException {
        logger.debug("Called getAvatar for user {}", userId);

        Instant version = userAccountService.getAvatarVersion(userId);
        String etag = "\"" + version.toEpochMilli() + "\"";

        // Si el cliente ya tiene esta versión no hace falta ir a S3
        if (request.checkNotModified(etag))
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .eTag(etag)
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(userAccountService.getAvatarContent(userId));
    }
}
