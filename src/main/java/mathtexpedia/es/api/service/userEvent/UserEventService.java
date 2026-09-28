package mathtexpedia.es.api.service.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.util.Map;

public interface UserEventService {

    void record(UserAccount user, EventType type, Map<String, Object> payload);

}
