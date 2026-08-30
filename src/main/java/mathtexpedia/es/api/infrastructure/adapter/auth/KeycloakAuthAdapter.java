package mathtexpedia.es.api.infrastructure.adapter.auth;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import mathtexpedia.es.api.domain.exception.AuthenticationException;
import mathtexpedia.es.api.domain.model.auth.AuthResult;
import mathtexpedia.es.api.domain.port.auth.AuthPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakAuthAdapter implements AuthPort {

    private final KeycloakTokenClient tokenClient;

    @Value("${app.auth.keycloak.realm}")
    private String realm;

    @Value("${app.auth.keycloak.client-id}")
    private String clientId;

    @Value("${app.auth.keycloak.client-secret}")
    private String clientSecret;

    @Override
    public AuthResult login(String username, String password) throws AuthenticationException {
        Map<String, String> form = baseForm();
        form.put("grant_type", "password");
        form.put("username", username);
        form.put("password", password);

        return callAndMap(form, "Credenciales inválidas");
    }

    @Override
    public AuthResult refreshToken(String refreshToken) throws AuthenticationException {
        Map<String, String> form = baseForm();
        form.put("grant_type", "refresh_token");
        form.put("refresh_token", refreshToken);

        return callAndMap(form, "Refresh token inválido o expirado");
    }

    @Override
    public void logout(String email, String refreshToken) throws AuthenticationException {
        Map<String, String> form = baseForm();
        form.put("refresh_token", refreshToken);

        try {
            tokenClient.logout(realm, form);
        } catch (FeignException e) {
            throw new AuthenticationException("Error al cerrar sesión", e);
        }
    }

    private AuthResult callAndMap(Map<String, String> form, String errorOnUnauthorized) {
        try {
            KeycloakTokenResponse response = tokenClient.getToken(realm, form);
            return toAuthResult(response);
        } catch (FeignException.Unauthorized | FeignException.BadRequest e) {
            throw new AuthenticationException(errorOnUnauthorized, e);
        } catch (FeignException e) {
            throw new AuthenticationException("Error de autenticación con el proveedor", e);
        }
    }

    private Map<String, String> baseForm() {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("client_id", clientId);
        form.put("client_secret", clientSecret);
        return form;
    }

    private AuthResult toAuthResult(KeycloakTokenResponse r) {
        return AuthResult.builder()
                .jwt(r.getAccessToken())
                .refreshToken(r.getRefreshToken())
                .expiresIn(r.getExpiresIn())
                .refreshExpiresIn(r.getRefreshExpiresIn())
                .tokenType(r.getTokenType())
                .build();
    }
}