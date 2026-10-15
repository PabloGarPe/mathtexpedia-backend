package mathtexpedia.es.api.domain.event;

import mathtexpedia.es.api.domain.model.notification.ResourceType;

public record ResourceDeletedEvent(ResourceType resourceType, long resourceId) {
}
