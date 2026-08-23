package mathtexpedia.es.api.presentation.profile;

import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController extends GenericController {

    @GetMapping
    public ResponseEntity<UserProfile> getProfile(@AuthenticationPrincipal UserProfile userProfile) {
        return ResponseEntity.ok(userProfile);
    }
}
