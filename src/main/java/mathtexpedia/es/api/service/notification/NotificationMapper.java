package mathtexpedia.es.api.service.notification;

import mathtexpedia.es.api.domain.model.notification.NotificationDto;
import mathtexpedia.es.api.persistence.notification.Notification;
import mathtexpedia.es.api.persistence.notification.NotificationDelivery;
import mathtexpedia.es.api.service.notification.render.NotificationRendererRegistry;
import mathtexpedia.es.api.service.notification.render.RenderedNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class NotificationMapper {

    private static final Logger logger = LoggerFactory.getLogger(NotificationMapper.class);

    private final NotificationRendererRegistry rendererRegistry;
    private final ObjectMapper objectMapper;

    public NotificationMapper(NotificationRendererRegistry rendererRegistry, ObjectMapper objectMapper) {
        this.rendererRegistry = rendererRegistry;
        this.objectMapper = objectMapper;
    }

    public NotificationDto toDto(NotificationDelivery delivery) {
        Notification notification = delivery.getNotification();
        RenderedNotification rendered = rendererRegistry.render(notification.getType(), readPayload(notification));

        return new NotificationDto(
                delivery.getId(),
                notification.getType(),
                notification.getType().category(),
                rendered.title(),
                rendered.message(),
                rendered.link(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getSubjectId(),
                delivery.getReadAt() != null,
                delivery.getReadAt(),
                delivery.getCreatedAt()
        );
    }

    private Map<String, Object> readPayload(Notification notification) {
        if (notification.getPayload() == null || notification.getPayload().isBlank())
            return Map.of();

        try {
            return objectMapper.readValue(notification.getPayload(), new TypeReference<Map<String, Object>>() {
            });
        } catch (JacksonException e) {
            logger.warn("Could not read payload of notification {}", notification.getId(), e);
            return Map.of();
        }
    }
}
