package mathtexpedia.es.api.presentation.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Perfil", description = "Datos del usuario autenticado")
@RestController
@RequestMapping("/profile")
public class ProfileController extends GenericController {

    @Operation(summary = "Obtiene el perfil del usuario autenticado",
            description = "Extrae el email y el rol a partir del JWT enviado en la petición")
    @GetMapping
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal UserProfile userProfile) {
        return ResponseEntity.ok(userProfile);
    }
}
