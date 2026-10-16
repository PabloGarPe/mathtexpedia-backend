package mathtexpedia.es.api.persistence.subjectSubscription;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubjectSubscriptionDataService {

    /** Suscripciones del usuario con su asignatura cargada, ordenadas por nombre de asignatura. */
    List<SubjectSubscription> getByUser(long userAccountId);

    Optional<SubjectSubscription> getByUserAndSubject(long userAccountId, long subjectId);

    /** Ids de las asignaturas a las que está suscrito el usuario. */
    Set<Long> getSubjectIdsByUser(long userAccountId);

    /**
     * Suscribe al usuario sin fallar si ya lo estaba (también ante dos peticiones simultáneas).
     *
     * @return true si se ha creado la suscripción, false si ya existía
     */
    boolean add(long userAccountId, long subjectId);

    /** @return true si existía y se ha borrado */
    boolean remove(long userAccountId, long subjectId);

    /**
     * Suscribe al usuario a todas las asignaturas colocadas ahora en ese grado y año (y cuatrimestre, si se indica), sin fallar
     * por las que ya tenía. Es una foto fija: las que se coloquen después no se suscriben solas.
     *
     * @return número de suscripciones nuevas
     */
    int addAll(long userAccountId, long degreeId, int year, Integer semester);

    /** Da de baja al usuario de las asignaturas colocadas ahora en ese curso, estén o no colocadas también en otro. @return las borradas */
    int removeAll(long userAccountId, long degreeId, int year, Integer semester);

    /** Suscripciones del usuario a las asignaturas colocadas ahora en ese curso, con su asignatura cargada y ordenadas por nombre. */
    List<SubjectSubscription> getByUserAndCourse(long userAccountId, long degreeId, int year, Integer semester);
}
