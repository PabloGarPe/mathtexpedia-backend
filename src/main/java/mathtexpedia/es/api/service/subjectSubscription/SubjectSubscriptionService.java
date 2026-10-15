package mathtexpedia.es.api.service.subjectSubscription;

import mathtexpedia.es.api.domain.exception.MathtexpediaNotFoundException;
import mathtexpedia.es.api.domain.exception.MathtexpediaUnauthorizedException;
import mathtexpedia.es.api.domain.model.subjectSubscription.SubjectSubscriptionDto;
import mathtexpedia.es.api.domain.security.UserProfile;

import java.util.List;

public interface SubjectSubscriptionService {

    List<SubjectSubscriptionDto> getMySubscriptions(UserProfile user) throws MathtexpediaUnauthorizedException;

    /** Idempotente: si ya estaba suscrito devuelve la suscripción existente. */
    SubjectSubscriptionDto subscribe(UserProfile user, long subjectId)
            throws MathtexpediaUnauthorizedException, MathtexpediaNotFoundException;

    /** Idempotente: si no estaba suscrito (o la asignatura no existe) no hace nada. */
    void unsubscribe(UserProfile user, long subjectId) throws MathtexpediaUnauthorizedException;
}
