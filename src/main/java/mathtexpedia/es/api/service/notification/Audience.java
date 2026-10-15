package mathtexpedia.es.api.service.notification;

import java.util.Set;

/**
 * A quién va dirigida una notificación, antes de aplicar preferencias y exclusiones.
 * Con las suscripciones (#70) aquí se añadirá {@code SubjectSubscribers(long subjectId)}.
 */
public sealed interface Audience permits Audience.AllUsers, Audience.Users {

    /** Todos los usuarios activos. */
    record AllUsers() implements Audience {
    }

    /** Un conjunto concreto de usuarios (ids internos). */
    record Users(Set<Long> userIds) implements Audience {
    }

    static Audience all() {
        return new AllUsers();
    }

    static Audience users(Set<Long> userIds) {
        return new Users(userIds);
    }
}
