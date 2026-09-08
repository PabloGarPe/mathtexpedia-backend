package mathtexpedia.es.api.service.auth;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.auth.*;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface AuthService {
    AuthResult refreshToken(String refreshToken);

    AuthResult login(String email, String password);

    void createUser(CreateUserRequest req);

    void sendResetPasswordEmail(ResetPasswordRequest req);

    void deleteUser(String email);

    List<UserDTO> findAllUsers();

    void changePassword(ChangePasswordRequest changePasswordRequest, UserProfile userProfile) throws MathtexpediaInvalidException;
}
