package mathtexpedia.es.api.presentation.mail;

import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.mail.Mail;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
public class MailController extends GenericController {

    @Autowired
    MailService mailService;

    @PostMapping("/send")
    public void sendMain(@AuthenticationPrincipal UserProfile user, @RequestBody @Valid Mail mail) throws PortActionNotPerformedException {
        logger.debug("Sending mail from user {}", user);
        mailService.sendMail(user, mail);
    }
}
