package mathtexpedia.es.api.domain.port.mail;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.mail.Mail;

public interface MailPort {

    void sendMail(Mail mail) throws PortActionNotPerformedException;
    void sendMail(Mail mail, String to) throws PortActionNotPerformedException;
}
