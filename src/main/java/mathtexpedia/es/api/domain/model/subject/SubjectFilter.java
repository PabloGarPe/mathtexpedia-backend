package mathtexpedia.es.api.domain.model.subject;

/**
 * Filtros opcionales del listado de asignaturas. {@code year} y {@code semester} exigen {@code degreeId};
 * {@code unassigned} no se puede combinar con {@code degreeId}.
 */
public record SubjectFilter(Long degreeId, Integer year, Integer semester, boolean unassigned) {
}
