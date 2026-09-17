package mathtexpedia.es.api.infrastructure.adapter.auth;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface KeycloakAdminClient {

    @RequestLine("POST /admin/realms/{realm}/users")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void createUser(@Param("realm") String realm, @Param("token") String token,
                    KeycloakUserRepresentation user);

    @RequestLine("GET /admin/realms/{realm}/users/{userId}")
    @Headers("Authorization: Bearer {token}")
    KeycloakUserRepresentation findById(@Param("realm") String realm, @Param("token") String token,
                                        @Param("userId") String userId);

    @RequestLine("GET /admin/realms/{realm}/users?email={email}&exact=true")
    @Headers("Authorization: Bearer {token}")
    List<KeycloakUserRepresentation> findByEmail(@Param("realm") String realm, @Param("token") String token,
                                                 @Param("email") String email);

    @RequestLine("GET /admin/realms/{realm}/users")
    @Headers("Authorization: Bearer {token}")
    List<KeycloakUserRepresentation> getAll(@Param("realm") String realm, @Param("token") String token);

    @RequestLine("GET /admin/realms/{realm}/roles/{roleName}")
    @Headers("Authorization: Bearer {token}")
    KeycloakRoleRepresentation getRealmRole(@Param("realm") String realm, @Param("token") String token,
                                            @Param("roleName") String roleName);

    @RequestLine("POST /admin/realms/{realm}/users/{userId}/role-mappings/realm")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void assignRealmRoles(@Param("realm") String realm, @Param("token") String token,
                          @Param("userId") String userId, List<KeycloakRoleRepresentation> roles);

    @RequestLine("GET /admin/realms/{realm}/users/{userId}/role-mappings/realm")
    @Headers("Authorization: Bearer {token}")
    List<KeycloakRoleRepresentation> getUserRealmRoles(@Param("realm") String realm, @Param("token") String token,
                                                       @Param("userId") String userId);

    @RequestLine("PUT /admin/realms/{realm}/users/{userId}/reset-password")
    @Headers({"Authorization: Bearer {token}", "Content-Type: application/json"})
    void resetPassword(@Param("realm") String realm, @Param("token") String token,
                       @Param("userId") String userId, CredentialRepresentation credential);

    @RequestLine("DELETE /admin/realms/{realm}/users/{userId}")
    @Headers("Authorization: Bearer {token}")
    void deleteUser(@Param("realm") String realm, @Param("token") String token,
                    @Param("userId") String userId);
}