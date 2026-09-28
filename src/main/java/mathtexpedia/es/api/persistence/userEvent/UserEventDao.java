package mathtexpedia.es.api.persistence.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.GenericJPADao;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserEventDao extends GenericJPADao implements UserEventDataService {

    @Override
    @Transactional
    public void record(UserAccount user, EventType type, String payload) {
        logger.trace("Recording user event for user {} with type {} and payload {}", user, type, payload);

        UserEvent userEvent = new UserEvent();
        userEvent.setUserAccount(user);
        userEvent.setEventType(type);
        userEvent.setPayload(payload);
        userEvent.setOccurredAt(java.time.Instant.now());

        em.persist(userEvent);
    }
}
