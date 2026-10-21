package mathtexpedia.es.api.persistence.subjectSubscription;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class SubjectSubscriptionDao extends GenericJPADao implements SubjectSubscriptionDataService {

    private static final String USER_PARAM = "userId";
    private static final String SUBJECT_PARAM = "subjectId";
    private static final String DEGREE_PARAM = "degreeId";
    private static final String YEAR_PARAM = "year";
    private static final String SEMESTER_PARAM = "semester";

    @Override
    @Transactional(readOnly = true)
    public List<SubjectSubscription> getByUser(long userAccountId) {
        logger.trace("Getting subscriptions of user {}", userAccountId);

        return em.createQuery("""
                        SELECT s FROM SubjectSubscription s JOIN FETCH s.subject sub
                        WHERE s.userAccount.id = :userId
                        ORDER BY sub.name, s.id
                        """, SubjectSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubjectSubscription> getByUserAndSubject(long userAccountId, long subjectId) {
        logger.trace("Getting subscription of user {} to subject {}", userAccountId, subjectId);

        return em.createQuery("""
                        SELECT s FROM SubjectSubscription s JOIN FETCH s.subject
                        WHERE s.userAccount.id = :userId AND s.subject.id = :subjectId
                        """, SubjectSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getSubjectIdsByUser(long userAccountId) {
        logger.trace("Getting subscribed subject ids of user {}", userAccountId);

        return new HashSet<>(em.createQuery(
                        "SELECT s.subject.id FROM SubjectSubscription s WHERE s.userAccount.id = :userId", Long.class)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList());
    }

    @Override
    @Transactional
    public boolean add(long userAccountId, long subjectId) {
        logger.trace("Subscribing user {} to subject {}", userAccountId, subjectId);

        return em.createNativeQuery("""
                        INSERT INTO subject_subscription (user_id, subject_id, created_at)
                        VALUES (:userId, :subjectId, :now)
                        ON CONFLICT (user_id, subject_id) DO NOTHING
                        """)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .setParameter("now", Instant.now())
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public boolean remove(long userAccountId, long subjectId) {
        logger.trace("Unsubscribing user {} from subject {}", userAccountId, subjectId);

        return em.createQuery("DELETE FROM SubjectSubscription s WHERE s.userAccount.id = :userId AND s.subject.id = :subjectId")
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(SUBJECT_PARAM, subjectId)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public int addAll(long userAccountId, long degreeId, int year, Integer semester) {
        logger.trace("Subscribing user {} to degree {} year {} (semester: {})", userAccountId, degreeId, year, semester);

        // El SQL se construye según haga falta en vez de pasar parámetros nulos (Postgres no infiere su tipo en "? IS NULL").
        StringBuilder sql = new StringBuilder("""
                INSERT INTO subject_subscription (user_id, subject_id, created_at)
                SELECT :userId, ds.subject_id, :now
                FROM degree_subject ds
                WHERE ds.degree_id = :degreeId AND ds.course_year = :year
                """);
        if (semester != null)
            sql.append("  AND ds.semester = :semester\n");
        sql.append("ON CONFLICT (user_id, subject_id) DO NOTHING");

        Query query = em.createNativeQuery(sql.toString())
                .setParameter(USER_PARAM, userAccountId)
                .setParameter("now", Instant.now())
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int addForCourseSubscribers(long subjectId, long degreeId, int year, Integer semester) {
        logger.trace("Subscribing subscribers of degree {} year {} (semester: {}) to subject {}", degreeId, year, semester, subjectId);

        StringBuilder sql = new StringBuilder("""
                INSERT INTO subject_subscription (user_id, subject_id, created_at)
                SELECT DISTINCT cs.user_id, :subjectId, :now
                FROM course_subscription cs JOIN user_account u ON u.id = cs.user_id
                WHERE cs.degree_id = :degreeId AND cs.course_year = :year AND u.status = 'ACTIVE'
                """);
        sql.append(semester != null ? "  AND (cs.semester IS NULL OR cs.semester = :semester)\n" : "  AND cs.semester IS NULL\n");
        sql.append("ON CONFLICT (user_id, subject_id) DO NOTHING");

        Query query = em.createNativeQuery(sql.toString())
                .setParameter(SUBJECT_PARAM, subjectId)
                .setParameter("now", Instant.now())
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.executeUpdate();
    }

    @Override
    @Transactional
    public int removeAll(long userAccountId, long degreeId, int year, Integer semester) {
        logger.trace("Unsubscribing user {} from degree {} year {} (semester: {})", userAccountId, degreeId, year, semester);

        Query query = em.createQuery("DELETE FROM SubjectSubscription s WHERE s.userAccount.id = :userId AND s.subject.id IN ("
                + courseSubjectIdsJpql(semester) + ")")
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectSubscription> getByUserAndCourse(long userAccountId, long degreeId, int year, Integer semester) {
        logger.trace("Getting subscriptions of user {} to degree {} year {} (semester: {})", userAccountId, degreeId, year, semester);

        TypedQuery<SubjectSubscription> query = em.createQuery(
                        "SELECT s FROM SubjectSubscription s JOIN FETCH s.subject sub WHERE s.userAccount.id = :userId AND sub.id IN ("
                                + courseSubjectIdsJpql(semester) + ") ORDER BY sub.name, s.id",
                        SubjectSubscription.class)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(DEGREE_PARAM, degreeId)
                .setParameter(YEAR_PARAM, year);
        if (semester != null)
            query.setParameter(SEMESTER_PARAM, semester);

        return query.getResultList();
    }

    /** Subconsulta con los ids de las asignaturas colocadas en un curso; el cuatrimestre solo se filtra si se indica. */
    private static String courseSubjectIdsJpql(Integer semester) {
        return "SELECT ds.subject.id FROM DegreeSubject ds WHERE ds.degree.id = :degreeId AND ds.year = :year"
                + (semester != null ? " AND ds.semester = :semester" : "");
    }
}
