package mathtexpedia.es.api.domain.event;

/** Un comentario se ha rechazado (por la IA o por un admin): hay que avisar a su autor. */
public record CommentRejectedEvent(long commentId) {
}
