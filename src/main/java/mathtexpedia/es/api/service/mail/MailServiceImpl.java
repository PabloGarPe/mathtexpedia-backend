package mathtexpedia.es.api.service.mail;

import mathtexpedia.es.api.domain.exception.PortActionNotPerformedException;
import mathtexpedia.es.api.domain.model.mail.Mail;
import mathtexpedia.es.api.domain.port.mail.MailPort;
import mathtexpedia.es.api.domain.security.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Autowired
    MailPort port;

    @Override
    public void sendMail(UserProfile user, Mail mail) throws PortActionNotPerformedException {

        if (mail.getFrom() == null)
            mail.setFrom(user.getEmail());

        if (!mail.getFrom().equalsIgnoreCase(user.getEmail()))
            mail.setCc(user.getEmail());

        port.sendMail(mail);
    }
}
