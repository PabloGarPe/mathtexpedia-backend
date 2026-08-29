package mathtexpedia.es.api.presentation.mail;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Mail", description = "Envío de correos en nombre del usuario autenticado")
@RestController
@RequestMapping("/mail")
public class MailController extends GenericController {

    @Autowired
    MailService mailService;

    @Operation(summary = "Envía un correo",
            description = "El remitente se rellena a partir del usuario autenticado si no se indica en el body")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo enviado correctamente"),
            @ApiResponse(responseCode = "500", description = "El proveedor de correo no pudo procesar el envío")
    })
    @PostMapping("/send")
    public void sendMain(@AuthenticationPrincipal UserProfile user, @RequestBody @Valid Mail mail) throws PortActionNotPerformedException {
        logger.debug("Sending mail from user {}", user);
        mailService.sendMail(user, mail);
    }
}
