package mathtexpedia.es.api.persistence.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.user.UserAccount;

public interface UserEventDataService {

    void record(UserAccount user, EventType type, String payload);

}
