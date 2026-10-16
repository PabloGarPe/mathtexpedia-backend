package mathtexpedia.es.api.domain.model.pdfRating;

/** Motivo por el que un usuario no puede votar un PDF. */
public enum CannotVoteReason {
    /** Todavía no ha abierto el PDF. */
    NOT_VIEWED,
    /** Es autor o coautor del PDF. */
    AUTHOR
}
