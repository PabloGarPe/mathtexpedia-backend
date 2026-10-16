package mathtexpedia.es.api.persistence.degreeSubject;

import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.persistence.GenericJPADao;
import mathtexpedia.es.api.persistence.degree.Degree;
import mathtexpedia.es.api.persistence.subject.Subject;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
public class DegreeSubjectDao extends GenericJPADao implements DegreeSubjectDataService {

    private static final String DEGREE_PARAM = "degreeId";
    private static final String YEAR_PARAM = "year";
    private static final String SEMESTER_PARAM = "semester";

    @Override
    @Transactional(readOnly = true)
    public List<DegreeSubject> getBySubjectIds(Collection<Long> subjectIds) {
        logger.trace("Getting placements of {} subjects", subjectIds == null ? 0 : subjectIds.size());

        if (subjectIds == null || subjectIds.isEmpty())
            return List.of();

        return em.createQuery("""
                        SELECT ds FROM DegreeSubject ds JOIN FETCH ds.degree d
                        WHERE ds.subject.id IN :subjectIds
                        ORDER BY d.name, ds.year
                        """, DegreeSubject.class)
                .setParameter("subjectIds", subjectIds)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DegreeSubject> getByDegree(long degreeId, Integer year, Integer semester) {
        logger.trace("Getting placements of degree {} (year: {}, semester: {})", degreeId, year, semester);

        // La consulta se construye según haga falta en vez de pasar parámetros nulos (Postgres no infiere su tipo)
        StringBuilder jpql = new StringBuilder("""
                SELECT ds FROM DegreeSubject ds JOIN FETCH ds.subject s
                WHERE ds.degree.id = :degreeId
                """);
        if (year != null)
            jpql.append("  AND ds.year = :year\n");
        if (semester != null)
            jpql.append("  AND ds.semester = :semester\n");
        jpql.append("ORDER BY ds.year, ds.semester NULLS LAST, s.name");

        TypedQuery<DegreeSubject> query = em.createQuery(jpql.toString(), DegreeSubject.class)
                .setParameter(DEGREE_PARAM, degreeId);
        if (year != null)
            query.setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getSubjectIds(long degreeId, int year, Integer semester) {
        logger.trace("Getting subject ids of degree {} (year: {}, semester: {})", degreeId, year, semester);

        StringBuilder jpql = new StringBuilder("""
                SELECT ds.subject.id FROM DegreeSubject ds
                WHERE ds.degree.id = :degreeId AND ds.year = :year
                """);
        if (semester != null)
            jpql.append("  AND ds.semester = :semester\n");

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByDegree(long degreeId) {
        logger.trace("Counting placements of degree {}", degreeId);

        return em.createQuery("SELECT COUNT(ds) FROM DegreeSubject ds WHERE ds.degree.id = :degreeId", Long.class)
                .setParameter(DEGREE_PARAM, degreeId)
                .getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getDegreeIdsForSubject(long subjectId) {
        logger.trace("Getting degree ids of subject {}", subjectId);

        return new HashSet<>(em.createQuery("SELECT ds.degree.id FROM DegreeSubject ds WHERE ds.subject.id = :subjectId", Long.class)
                .setParameter("subjectId", subjectId)
                .getResultList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> maxYearForDegree(long degreeId) {
        logger.trace("Getting max placement year of degree {}", degreeId);

        return Optional.ofNullable(em.createQuery("SELECT MAX(ds.year) FROM DegreeSubject ds WHERE ds.degree.id = :degreeId", Integer.class)
                .setParameter(DEGREE_PARAM, degreeId)
                .getSingleResult());
    }

    @Override
    @Transactional
    public void replaceForSubject(long subjectId, List<DegreeSubject> placements) {
        logger.trace("Replacing placements of subject {} with {} new ones", subjectId, placements.size());

        em.createQuery("DELETE FROM DegreeSubject ds WHERE ds.subject.id = :subjectId")
                .setParameter("subjectId", subjectId)
                .executeUpdate();
        // Los borrados deben llegar a la base de datos antes que los inserts, o chocaría la unicidad (grado, asignatura)
        em.flush();

        for (DegreeSubject placement : placements) {
            placement.setDegree(em.getReference(Degree.class, placement.getDegree().getId()));
            placement.setSubject(em.getReference(Subject.class, subjectId));
            em.persist(placement);
        }
    }
}
