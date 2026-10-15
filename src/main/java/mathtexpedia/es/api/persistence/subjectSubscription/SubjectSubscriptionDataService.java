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
}
