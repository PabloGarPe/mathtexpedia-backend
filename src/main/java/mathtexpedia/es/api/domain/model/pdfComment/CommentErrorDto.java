package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentErrorDto(
        @Schema(description = "EMPTY, TOO_LONG, LINKS_NOT_ALLOWED, IMAGES_NOT_ALLOWED o HTML_NOT_ALLOWED", example = "LINKS_NOT_ALLOWED")
        String code) {
}
