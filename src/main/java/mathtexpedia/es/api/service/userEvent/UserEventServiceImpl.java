package mathtexpedia.es.api.service.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.user.UserAccount;
import mathtexpedia.es.api.persistence.userEvent.UserEventDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class UserEventServiceImpl implements UserEventService{

    private final static Logger logger = LoggerFactory.getLogger(UserEventServiceImpl.class);

    private final UserEventDataService userEventDataService;
    private final ObjectMapper objectMapper;

    public UserEventServiceImpl(UserEventDataService userEventDataService, ObjectMapper objectMapper) {
        this.userEventDataService = userEventDataService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(UserAccount user, EventType type, Map<String, Object> payload) {
        logger.info("Recording user event: user={}, type={}, payload={}", user.getId(), type, payload);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            logger.error("Error serializing payload to JSON, skipping event recording", e);
            return;
        }

        try {
            userEventDataService.record(user, type, payloadJson);
        } catch (Exception e) {
            logger.error("Error recording user event", e);
        }
    }
}
