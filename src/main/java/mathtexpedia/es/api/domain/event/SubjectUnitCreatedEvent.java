package mathtexpedia.es.api.domain.event;

/** Un tema nuevo en una asignatura: se notifica solo a los suscritos a ella. */
public record SubjectUnitCreatedEvent(long subjectUnitId, long subjectId, String actorExternalId) {
}
