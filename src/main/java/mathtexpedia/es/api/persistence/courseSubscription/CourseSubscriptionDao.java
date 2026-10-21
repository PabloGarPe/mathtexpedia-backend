package mathtexpedia.es.api.persistence.courseSubscription;

import jakarta.persistence.Query;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class CourseSubscriptionDao extends GenericJPADao implements CourseSubscriptionDataService {

    private static final String USER_PARAM = "userId";
    private static final String DEGREE_PARAM = "degreeId";
    private static final String YEAR_PARAM = "year";
    private static final String SEMESTER_PARAM = "semester";

    @Override
    @Transactional(readOnly = true)
    public List<CourseSubscription> getByUser(long userAccountId) {
        logger.trace("Getting course subscriptions of user {}", userAccountId);

        return em.createQuery("""
                        SELECT cs FROM CourseSubscription cs JOIN FETCH cs.degree d
                        WHERE cs.userAccount.id = :userId
                        ORDER BY d.name, cs.year, cs.semester NULLS FIRST, cs.id
                        """, CourseSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList();
    }

    @Override
    @Transactional
    public void add(long userAccountId, long degreeId, int year, Integer semester) {
        logger.trace("Adding course subscription of user {} to degree {} year {} (semester: {})", userAccountId, degreeId, year, semester);

        lockUser(userAccountId);
        if (semester == null) {
            if (exists(userAccountId, degreeId, year, null))
                return;
            delete(userAccountId, degreeId, year, false, null);
            insert(userAccountId, degreeId, year, null);
        } else if (!exists(userAccountId, degreeId, year, null) && !exists(userAccountId, degreeId, year, semester)) {
            insert(userAccountId, degreeId, year, semester);
        }
    }

    @Override
    @Transactional
    public void remove(long userAccountId, long degreeId, int year, Integer semester) {
        logger.trace("Removing course subscription of user {} to degree {} year {} (semester: {})", userAccountId, degreeId, year, semester);

        lockUser(userAccountId);
        if (semester == null) {
            delete(userAccountId, degreeId, year, false, null);
            return;
        }

        delete(userAccountId, degreeId, year, true, semester);
        if (delete(userAccountId, degreeId, year, true, null) > 0)
            insert(userAccountId, degreeId, year, 3 - semester);
    }

    @Override
    @Transactional
    public void removeAboveYear(long degreeId, int years) {
        logger.trace("Removing course subscriptions of degree {} above year {}", degreeId, years);

        em.createQuery("DELETE FROM CourseSubscription cs WHERE cs.degree.id = :degreeId AND cs.year > :years")
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter("years", years)
                .executeUpdate();
    }

    private void lockUser(long userAccountId) {
        em.createNativeQuery("SELECT 1 FROM pg_advisory_xact_lock(:userId)")
                .setParameter(USER_PARAM, userAccountId)
                .getSingleResult();
    }

    private boolean exists(long userAccountId, long degreeId, int year, Integer semester) {
        Query query = em.createQuery("SELECT COUNT(cs) FROM CourseSubscription cs WHERE cs.userAccount.id = :userId "
                        + "AND cs.degree.id = :degreeId AND cs.year = :year AND " + semesterJpql(semester))
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return (Long) query.getSingleResult() > 0;
    }

    private int delete(long userAccountId, long degreeId, int year, boolean exact, Integer semester) {
        Query query = em.createQuery("DELETE FROM CourseSubscription cs WHERE cs.userAccount.id = :userId "
                        + "AND cs.degree.id = :degreeId AND cs.year = :year" + (exact ? " AND " + semesterJpql(semester) : ""))
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (exact && semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.executeUpdate();
    }

    private void insert(long userAccountId, long degreeId, int year, Integer semester) {
        Query query = em.createNativeQuery("INSERT INTO course_subscription (user_id, degree_id, course_year, semester, created_at) "
                        + "VALUES (:userId, :degreeId, :year, " + (semester != null ? ":semester" : "NULL") + ", :now)")
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year)
                .setParameter("now", Instant.now());
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        query.executeUpdate();
    }

    private static String semesterJpql(Integer semester) {
        return semester != null ? "cs.semester = :semester" : "cs.semester IS NULL";
    }
}
