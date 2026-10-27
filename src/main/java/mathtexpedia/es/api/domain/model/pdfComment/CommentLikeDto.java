package mathtexpedia.es.api.domain.model.pdfComment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentLikeDto {

    @Schema(description = "Id del comentario", example = "12")
    private long commentId;

    @Schema(description = "Likes del comentario tras la operación", example = "5")
    private long likeCount;

    @Schema(description = "Si al usuario autenticado le gusta el comentario", example = "true")
    private boolean liked;
}
