package mathtexpedia.es.api.presentation.auth;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.auth.ChangePasswordRequest;
import mathtexpedia.es.api.domain.model.auth.UserDTO;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("auth")
public class PrivateAuthController extends GenericController {

    // TODO: Cambiar todo lo de los port por service
    @Autowired
    AuthService authService;

    @DeleteMapping("/delete-account")
    public void deleteAccount(@AuthenticationPrincipal UserProfile user) {
        logger.debug("Deleting account {}", user);

        authService.deleteUser(user);
    }

    @GetMapping("/all-users")
    public List<UserDTO> getAllUsers(@AuthenticationPrincipal UserProfile user) throws MathtexpediaUnauthorizedException {
        logger.debug("Getting all users {}", user);

        checkIfAdmin(user);
        return authService.findAllUsers();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, @AuthenticationPrincipal UserProfile user) throws MathtexpediaInvalidException {
        logger.debug("Changing password {}", changePasswordRequest);

        authService.changePassword(changePasswordRequest, user);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
