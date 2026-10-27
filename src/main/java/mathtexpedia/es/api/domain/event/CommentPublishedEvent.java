package mathtexpedia.es.api.domain.event;

/** Un comentario se ha publicado por primera vez (restaurar uno ya publicado no genera este evento). */
public record CommentPublishedEvent(long commentId) {
}
