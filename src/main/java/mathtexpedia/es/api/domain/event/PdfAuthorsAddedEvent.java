package mathtexpedia.es.api.domain.event;

import java.util.List;

public record PdfAuthorsAddedEvent(long pdfId, List<Long> addedAuthorIds, String actorExternalId) {
}
