package mathtexpedia.es.api.service.subjectSubscription;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.CourseSubscriptionDto;
import mathtexpedia.es.api.domain.model.subjectSubscription.SubjectSubscriptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface SubjectSubscriptionService {

    List<SubjectSubscriptionDto> getMySubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException;

    /** Cursos a los que está suscrito, ordenados por nombre del grado, año y cuatrimestre (el año entero primero). */
    List<CourseSubscriptionDto> getMyCourseSubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException;

    /** Idempotente: si ya estaba suscrito devuelve la suscripción existente. */
    SubjectSubscriptionDto subscribe(UserProfile user, long subjectId)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException;

    /** Idempotente: si no estaba suscrito (o la asignatura no existe) no hace nada. */
    void unsubscribe(UserProfile user, long subjectId) throws MathtexpediaUnauthorizedException;

    /**
     * Suscribe a las asignaturas colocadas ahora en (grado, año[, cuatrimestre]) y recuerda la suscripción al curso, para que las
     * que se coloquen después en él se suscriban solas. Idempotente. Devuelve las suscripciones del usuario a las asignaturas de ese curso (las nuevas y
     * las que ya tenía); lista vacía si el curso no tiene asignaturas.
     */
    List<SubjectSubscriptionDto> subscribeToCourse(UserProfile user, long degreeId, int year, Integer semester)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException;

    /**
     * Da de baja de las asignaturas colocadas ahora en ese curso, aunque estén colocadas también en otro curso al que el
     * usuario siga suscrito, y olvida la suscripción al curso. Idempotente.
     */
    void unsubscribeFromCourse(UserProfile user, long degreeId, int year, Integer semester)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException, MathtexpediaInvalidException;
}
