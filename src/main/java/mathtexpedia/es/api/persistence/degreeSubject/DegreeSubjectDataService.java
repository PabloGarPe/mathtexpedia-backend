package mathtexpedia.es.api.persistence.degreeSubject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DegreeSubjectDataService {

    /** Colocaciones (con su grado cargado) de las asignaturas dadas. Vacío si no se pasa ningún id. */
    List<DegreeSubject> getBySubjectIds(Collection<Long> subjectIds);

    /** Colocaciones (con su asignatura cargada) de un grado, filtradas opcionalmente por año y cuatrimestre. */
    List<DegreeSubject> getByDegree(long degreeId, Integer year, Integer semester);

    /** Ids de las asignaturas colocadas ahora en ese grado y año (y cuatrimestre, si se indica). */
    List<Long> getSubjectIds(long degreeId, int year, Integer semester);

    /** Ids de los grados en los que está colocada la asignatura (solo ids: sirve fuera de una sesión abierta). */
    Set<Long> getDegreeIdsForSubject(long subjectId);

    long countByDegree(long degreeId);

    /** Año más alto en el que hay alguna asignatura colocada en el grado, o vacío si no tiene colocaciones. */
    Optional<Integer> maxYearForDegree(long degreeId);

    /** Reemplaza todas las colocaciones de la asignatura por las dadas. */
    void replaceForSubject(long subjectId, List<DegreeSubject> placements);
}
