package mathtexpedia.es.api.presentation.auth;

import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.auth.UserDTO;
import mathtexpedia.es.api.domain.port.auth.UserManagementPort;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("auth")
public class PrivateAuthController extends GenericController {

    // TODO: Cambiar todo lo de los port por service
    @Autowired
    UserManagementPort userManagementPort;

    @DeleteMapping("/delete-account")
    public void deleteAccount(@AuthenticationPrincipal UserProfile user) {
        logger.debug("Deleting account {}", user);
        userManagementPort.deleteUser(user.getEmail());
    }

    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers(@AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.debug("Getting all users {}", user);
        checkIfAdmin(user);
        return userManagementPort.findAllUsers();
    }
}
