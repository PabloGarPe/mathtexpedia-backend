package mathtexpedia.es.api.infrastructure.adapter.mail;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.mail.Mail;
import mathtexpedia.es.api.domain.port.mail.MailPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResendAdapter implements MailPort {

    public static final String TO_EMAIL = "mathtexpedia@mathtexpedia.es";
    public static final String FROM_EMAIL = "Formulario Mathtexpedia <mathtexpedia@updates.mathtexpedia.es>";

    @Value("${resend.api.key}")
    private String apiKey;

    @Override
    public void sendMail(Mail mail) throws PortActionNotPerformedException {
        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(FROM_EMAIL)
                .to(TO_EMAIL)
                .addCc(mail.getFrom())
                .addCc(mail.getCc())
                .subject(mail.getSubject())
                .html(mail.getBody()).build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new PortActionNotPerformedException(e.getMessage());
        }
    }
}
