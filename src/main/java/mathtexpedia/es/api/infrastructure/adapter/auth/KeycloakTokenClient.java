package mathtexpedia.es.api.infrastructure.adapter.auth;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.Map;

public interface KeycloakTokenClient {

    @RequestLine("POST /realms/{realm}/protocol/openid-connect/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    KeycloakTokenResponse getToken(@Param("realm") String realm, Map<String, ?> formParams);

    @RequestLine("POST /realms/{realm}/protocol/openid-connect/logout")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    void logout(@Param("realm") String realm, Map<String, ?> formParams);
}