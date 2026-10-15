package mathtexpedia.es.api.domain.event;

import java.util.List;

/** Aviso manual de un administrador. {@code recipientUserIds} nulo o vacío significa todos los usuarios activos. */
public record AnnouncementCreatedEvent(String title, String message, String link,
                                       List<Long> recipientUserIds, String actorExternalId) {
}
