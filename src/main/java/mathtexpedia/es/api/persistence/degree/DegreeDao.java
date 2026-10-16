package mathtexpedia.es.api.persistence.degree;

import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DegreeDao extends GenericJPADao implements DegreeDataService {

    @Override
    @Transactional(readOnly = true)
    public List<Degree> getAll() {
        logger.trace("Getting all degrees");

        return em.createQuery("SELECT d FROM Degree d ORDER BY d.name, d.university NULLS FIRST, d.id", Degree.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Degree> getById(long id) {
        logger.trace("Getting degree with id {}", id);

        return Optional.ofNullable(em.find(Degree.class, id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndUniversity(String name, String university, Long excludedId) {
        logger.trace("Checking if degree '{}' of university '{}' exists (excluding {})", name, university, excludedId);

        // La consulta se construye según haga falta en vez de pasar parámetros nulos (Postgres no infiere su tipo)
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(d) FROM Degree d
                WHERE lower(d.name) = lower(:name)
                  AND COALESCE(lower(d.university), '') = lower(:university)
                """);
        if (excludedId != null)
            jpql.append("  AND d.id <> :excludedId\n");

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("name", name)
                .setParameter("university", university == null ? "" : university);
        if (excludedId != null)
            query.setParameter("excludedId", excludedId);

        return query.getSingleResult() > 0;
    }

    @Override
    @Transactional
    public Degree create(Degree degree) {
        logger.trace("Creating new degree {}", degree);

        em.persist(degree);
        return degree;
    }

    @Override
    @Transactional
    public Degree update(Degree degree) {
        logger.trace("Updating degree {}", degree);

        Degree merged = em.merge(degree);
        // Para que los errores de base de datos salten aquí y no al hacer commit fuera del DAO
        em.flush();
        return merged;
    }

    @Override
    @Transactional
    public void delete(Degree degree) {
        logger.trace("Deleting degree {}", degree);

        // Los usuarios que lo tenían en el perfil se quedan sin grado ni curso (la FK no lo permitiría de otro modo)
        em.createNativeQuery("UPDATE user_account SET degree_id = NULL, current_year = NULL WHERE degree_id = :id")
                .setParameter("id", degree.getId())
                .executeUpdate();
        em.remove(em.contains(degree) ? degree : em.merge(degree));
    }

    @Override
    @Transactional
    public void clearUsersCurrentYearAbove(long degreeId, int years) {
        logger.trace("Clearing current year above {} of users of degree {}", years, degreeId);

        em.createNativeQuery("UPDATE user_account SET current_year = NULL WHERE degree_id = :id AND current_year > :years")
                .setParameter("id", degreeId)
                .setParameter("years", years)
                .executeUpdate();
    }
}
