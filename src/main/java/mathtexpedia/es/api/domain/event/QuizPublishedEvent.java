package mathtexpedia.es.api.domain.event;

/** Un cuestionario se hace visible por primera vez. */
public record QuizPublishedEvent(long quizId, String actorExternalId) {
}
