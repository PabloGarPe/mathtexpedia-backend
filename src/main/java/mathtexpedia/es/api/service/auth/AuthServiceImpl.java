package mathtexpedia.es.api.service.auth;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.model.auth.*;
import mathtexpedia.es.api.domain.port.auth.AuthPort;
import mathtexpedia.es.api.domain.port.auth.UserManagementPort;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    AuthPort authPort;

    @Autowired
    UserManagementPort userManagementPort;

    @Override
    public AuthResult refreshToken(String refreshToken) {
        return  authPort.refreshToken(refreshToken);
    }

    @Override
    public AuthResult login(String email, String password) {
        return  authPort.login(email, password);
    }

    @Override
    public void createUser(CreateUserRequest req) {
        userManagementPort.createUser(req);
    }

    @Override
    public void sendResetPasswordEmail(ResetPasswordRequest req) {
        userManagementPort.sendResetPasswordEmail(req);
    }

    @Override
    public void deleteUser(String email) {
        userManagementPort.deleteUser(email);
    }

    @Override
    public List<UserDTO> findAllUsers() {
        return userManagementPort.findAllUsers();
    }

    @Override
    public void changePassword(ChangePasswordRequest request, UserProfile user) throws MathtexpediaInvalidException {
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new MathtexpediaInvalidException("Passwords must not be the same");
        }

        try {
            authPort.login(user.getEmail(), request.getOldPassword());
        } catch (Exception e) {
            throw new MathtexpediaInvalidException(e.getMessage());
        }

        userManagementPort.replacePassword(request, user.getEmail());

    }
}
