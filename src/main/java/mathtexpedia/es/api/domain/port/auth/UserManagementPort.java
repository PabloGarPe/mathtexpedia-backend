package mathtexpedia.es.api.domain.port.auth;

import mathtexpedia.es.api.domain.model.auth.CreateUserRequest;
import mathtexpedia.es.api.domain.model.auth.ResetPasswordRequest;
import mathtexpedia.es.api.domain.model.auth.UserDTO;
import mathtexpedia.es.api.domain.security.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserManagementPort {
    UserDTO createUser(CreateUserRequest command);
    Optional<UserDTO> findById(String userId);
    void assignRole(String userId, String role);
    List<UserRole> getUserRoles(String userId);
    void changePassword(ResetPasswordRequest command);
    void sendResetPasswordEmail(ResetPasswordRequest command);
    void deleteUser(String userEmail);
    List<UserDTO> findAllUsers();
}
