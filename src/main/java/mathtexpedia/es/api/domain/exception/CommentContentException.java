package mathtexpedia.es.api.domain.exception;

import lombok.Getter;
import mathtexpedia.es.api.domain.model.pdfComment.CommentContentError;

/** El texto del comentario no cumple las reglas de formato. Lleva el motivo para que el front lo explique. */
@Getter
public class CommentContentException extends MathtexpediaInvalidException {

    private final CommentContentError error;

    public CommentContentException(CommentContentError error) {
        super("Invalid comment content: " + error);
        this.error = error;
    }

}
