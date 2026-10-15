package mathtexpedia.es.api.domain.event;

import java.util.List;

public record PdfCreatedEvent(long pdfId, List<Long> authorIds, String actorExternalId) {
}
