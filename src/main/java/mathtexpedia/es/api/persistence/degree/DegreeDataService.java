package mathtexpedia.es.api.persistence.degree;

import java.util.List;
import java.util.Optional;

public interface DegreeDataService {

    List<Degree> getAll();

    Optional<Degree> getById(long id);

    /** Comprueba sin distinguir mayúsculas si ya hay un grado con ese nombre y universidad (nula = sin universidad). */
    boolean existsByNameAndUniversity(String name, String university, Long excludedId);

    Degree create(Degree degree);

    Degree update(Degree degree);

    /** Borra el grado y deja sin grado ni curso a los usuarios que lo tenían en su perfil. */
    void delete(Degree degree);

    /** Deja sin curso a los usuarios del grado cuyo curso actual supera {@code years} (al reducir los años del grado). */
    void clearUsersCurrentYearAbove(long degreeId, int years);
}
