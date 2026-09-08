package mathtexpedia.es.api.domain.port.auth;

import mathtexpedia.es.api.domain.exception.AuthenticationException;
import mathtexpedia.es.api.domain.model.auth.AuthResult;

public interface AuthPort {
    AuthResult login(String username, String password) throws AuthenticationException;
    AuthResult refreshToken(String refreshToken) throws AuthenticationException;
    void logout(String email, String refreshToken) throws AuthenticationException;
}
