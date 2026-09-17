package mathtexpedia.es.api.infrastructure.adapter.auth;

import lombok.RequiredArgsConstructor;
import mathtexpedia.es.api.domain.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakAdminTokenProvider {

    private final KeycloakTokenClient tokenClient;

    @Value("${app.auth.keycloak.realm}")
    private String realm;

    @Value("${app.auth.keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${app.auth.keycloak.admin-client-secret}")
    private String adminClientSecret;

    public String getAdminToken() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "client_credentials");
        form.put("client_id", adminClientId);
        form.put("client_secret", adminClientSecret);

        try {
            return tokenClient.getToken(realm, form).getAccessToken();
        } catch (Exception e) {
            throw new AuthenticationException("No se pudo obtener token de administración", e);
        }
    }
}