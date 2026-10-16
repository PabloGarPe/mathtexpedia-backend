package mathtexpedia.es.api.service.notification;

import java.util.Set;

/**
 * A quién va dirigida una notificación, antes de aplicar preferencias y exclusiones.
 */
public sealed interface Audience permits Audience.AllUsers, Audience.Users, Audience.SubjectSubscribers,
        Audience.DegreeMembersOrUnassigned {

    /** Todos los usuarios activos. */
    record AllUsers() implements Audience {
    }

    /** Un conjunto concreto de usuarios (ids internos). */
    record Users(Set<Long> userIds) implements Audience {
    }

    /** Los usuarios suscritos a una asignatura. */
    record SubjectSubscribers(long subjectId) implements Audience {
    }

    /** Los usuarios de alguno de estos grados y los que no tienen grado en su perfil. */
    record DegreeMembersOrUnassigned(Set<Long> degreeIds) implements Audience {
    }

    static Audience all() {
        return new AllUsers();
    }

    static Audience users(Set<Long> userIds) {
        return new Users(userIds);
    }

    static Audience subjectSubscribers(long subjectId) {
        return new SubjectSubscribers(subjectId);
    }

    static Audience degreeMembersOrUnassigned(Set<Long> degreeIds) {
        return new DegreeMembersOrUnassigned(degreeIds);
    }
}
