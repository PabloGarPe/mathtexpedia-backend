package mathtexpedia.es.api.persistence.subject;

import java.util.List;
import java.util.Optional;

public interface SubjectDataService {

    List<Subject> getAll();

    /**
     * Asignaturas ordenadas por nombre, filtradas opcionalmente por colocación: las de un grado (y, si se indican, un año y
     * un cuatrimestre dentro de él) o, con {@code unassigned}, las que no están colocadas en ningún grado.
     */
    List<Subject> getFiltered(Long degreeId, Integer year, Integer semester, boolean unassigned);

    Optional<Subject> getById(long id);

    Subject create(Subject subject);

    Subject update(Subject subject);

    void delete(Subject subject);
}
