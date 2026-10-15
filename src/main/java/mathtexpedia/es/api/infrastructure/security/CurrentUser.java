package mathtexpedia.es.api.infrastructure.security;

import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Usuario autenticado de la petición en curso. Se usa para saber quién provoca un evento (y no notificarle a él mismo)
 * sin tener que cambiar las firmas de los servicios. Debe llamarse en el hilo de la petición, nunca desde un listener async.
 */
@Component
public class CurrentUser {

    public Optional<String> externalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserProfile profile)
            return Optional.ofNullable(profile.getId());
        return Optional.empty();
    }

    /** Id externo del usuario autenticado o {@code null} si no hay (p. ej. tareas internas). */
    public String externalIdOrNull() {
        return externalId().orElse(null);
    }
}
