package mathtexpedia.es.api.service.mail;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.mail.Mail;
import mathtexpedia.es.api.domain.security.UserProfile;

public interface MailService {

    void sendMail(UserProfile user, Mail mail) throws PortActionNotPerformedException;
}
