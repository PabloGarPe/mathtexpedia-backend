package mathtexpedia.es.api.presentation.auth;

import mathtexpedia.es.api.domain.model.auth.*;
import mathtexpedia.es.api.infrastructure.application.PublicEndpoint;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("auth")
public class AuthController extends GenericController implements PublicEndpoint {

    // TODO: Cambiar todo lo de los puertos por service
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> signIn(@RequestBody LoginDTO loginDTO, @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        logger.debug("Request received for logging into mathtexpedia");

        AuthResult res = refreshToken != null
                ? authService.refreshToken(refreshToken)
                : authService.login(loginDTO.getEmail(), loginDTO.getPassword());

        ResponseCookie accessCookie = ResponseCookie.from("jwt", res.getJwt())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", res.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createUser(@RequestBody CreateUserRequest req) {
        logger.debug("Request received for registering into mathtexpedia");
        authService.createUser(req);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        logger.debug("Request received for resetting into mathtexpedia");
        authService.sendResetPasswordEmail(req);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
