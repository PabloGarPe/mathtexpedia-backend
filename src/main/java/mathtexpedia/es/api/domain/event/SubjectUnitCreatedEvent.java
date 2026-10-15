package mathtexpedia.es.api.domain.event;

/**
 * Se publica a propósito sin ningún listener: los temas nuevos solo se notificarán a los suscritos a la asignatura
 * (suscripciones, issue #70).
 */
public record SubjectUnitCreatedEvent(long subjectUnitId, long subjectId, String actorExternalId) {
}
