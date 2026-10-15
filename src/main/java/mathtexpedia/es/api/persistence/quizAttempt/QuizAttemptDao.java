package mathtexpedia.es.api.persistence.quizAttempt;

import jakarta.persistence.TypedQuery;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class QuizAttemptDao extends GenericJPADao implements QuizAttemptDataService {

    private static final String USER_ID_PARAM = "userId";
    private static final String QUIZ_ID_PARAM = "quizId";

    private static final String FIND_BY_USER_ID_ASC =
            "SELECT a FROM QuizAttempt a WHERE a.user.id = :userId ORDER BY a.submittedAt ASC";
    private static final String FIND_BY_USER_ID_DESC =
            "SELECT a FROM QuizAttempt a WHERE a.user.id = :userId ORDER BY a.submittedAt DESC";

    @Override
    @Transactional
    public QuizAttempt createQuizAttempt(QuizAttempt quizAttempt) {
        logger.trace("Creating quiz attempt: {}", quizAttempt);

        em.persist(quizAttempt);
        return quizAttempt;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuizAttempt> getByUserId(long userId, Pageable pageable) {
        logger.trace("Getting quiz attempts for user: {}", userId);

        Sort.Order submittedAtOrder = pageable.getSort().getOrderFor("submittedAt");
        boolean ascending = submittedAtOrder != null && submittedAtOrder.isAscending();

        TypedQuery<QuizAttempt> query = em.createQuery(
                ascending ? FIND_BY_USER_ID_ASC : FIND_BY_USER_ID_DESC,
                QuizAttempt.class
        );
        query.setParameter(USER_ID_PARAM, userId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = em.createQuery("SELECT COUNT(a) FROM QuizAttempt a WHERE a.user.id = :userId", Long.class)
                .setParameter(USER_ID_PARAM, userId)
                .getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizAttempt> getByQuizIdAndUserId(long quizId, long userId) {
        logger.trace("Getting quiz attempts for quiz: {} and user: {}", quizId, userId);

        TypedQuery<QuizAttempt> query = em.createQuery(
                "SELECT a FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.user.id = :userId ORDER BY a.submittedAt DESC",
                QuizAttempt.class
        );
        query.setParameter(QUIZ_ID_PARAM, quizId);
        query.setParameter(USER_ID_PARAM, userId);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizAttemptStats getStatsByUserId(long userId) {
        logger.trace("Getting quiz attempt stats for user: {}", userId);

        TypedQuery<QuizAttemptStats> query = em.createQuery(
                "SELECT new mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptStats(" +
                        "COUNT(a), " +
                        "SUM(CASE WHEN a.unansweredQuestions = 0 THEN 1 ELSE 0 END), " +
                        "SUM(a.correctAnswers), " +
                        "SUM(a.totalQuestions - a.unansweredQuestions), " +
                        "AVG(a.score), " +
                        "MAX(a.score)) " +
                        "FROM QuizAttempt a WHERE a.user.id = :userId",
                QuizAttemptStats.class
        );
        query.setParameter(USER_ID_PARAM, userId);
        return query.getSingleResult();
    }
    @Transactional(readOnly = true)
    public QuizAttemptStats getStatsByQuizIdAndUserId(long quizId, long userId) {
        logger.trace("Getting quiz attempt stats for quiz: {} and user: {}", quizId, userId);

        TypedQuery<QuizAttemptStats> query = em.createQuery(
                "SELECT new mathtexpedia.es.api.persistence.quizAttempt.QuizAttemptStats(" +
                        "COUNT(a), " +
                        "SUM(CASE WHEN a.unansweredQuestions = 0 THEN 1 ELSE 0 END), " +
                        "SUM(a.correctAnswers), " +
                        "SUM(a.totalQuestions - a.unansweredQuestions), " +
                        "AVG(a.score), " +
                        "MAX(a.score)) " +
                        "FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.user.id = :userId",
                QuizAttemptStats.class
        );
        query.setParameter(QUIZ_ID_PARAM, quizId);
        query.setParameter(USER_ID_PARAM, userId);
        return query.getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByQuizIdAndUserId(long quizId, long userId) {
        logger.trace("Counting quiz attempts for quiz: {} and user: {}", quizId, userId);

        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(a) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.user.id = :userId",
                Long.class
        );
        query.setParameter(QUIZ_ID_PARAM, quizId);
        query.setParameter(USER_ID_PARAM, userId);
        return query.getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardRow> getLeaderboardForQuiz(long quizId, Pageable pageable) {
        TypedQuery<LeaderboardRow> query = em.createQuery(
                "SELECT new mathtexpedia.es.api.persistence.quizAttempt.LeaderboardRow(" +
                        "a.user.id, a.user.name, MAX(a.adjustedScore)) " +
                        "FROM QuizAttempt a WHERE a.quiz.id = :quizId " +
                        "GROUP BY a.user.id, a.user.name " +
                        "ORDER BY MAX(a.adjustedScore) DESC",
                LeaderboardRow.class
        );
        query.setParameter(QUIZ_ID_PARAM, quizId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long totalUsers = em.createQuery(
                "SELECT COUNT(DISTINCT a.user.id) FROM QuizAttempt a WHERE a.quiz.id = :quizId",
                Long.class
        ).setParameter(QUIZ_ID_PARAM, quizId).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, totalUsers);
    }

    @Override
    @Transactional
    public void deleteByQuizId(long quizId) {
        logger.trace("Deleting quiz attempts of quiz with id {}", quizId);

        em.createQuery("DELETE FROM QuizAttempt a WHERE a.quiz.id = :quizId")
                .setParameter(QUIZ_ID_PARAM, quizId)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countAttemptsByQuizIds(Collection<Long> quizIds) {
        if (quizIds == null || quizIds.isEmpty())
            return Map.of();

        logger.trace("Counting quiz attempts for {} quizzes", quizIds.size());

        List<Object[]> rows = em.createQuery(
                "SELECT a.quiz.id, COUNT(a) FROM QuizAttempt a WHERE a.quiz.id IN :quizIds GROUP BY a.quiz.id",
                Object[].class
        ).setParameter("quizIds", quizIds).getResultList();

        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows)
            counts.put((Long) row[0], (Long) row[1]);
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getMostAttemptedQuizIds(int limit) {
        logger.trace("Getting top {} most attempted quizzes", limit);

        return em.createQuery(
                "SELECT a.quiz.id FROM QuizAttempt a GROUP BY a.quiz.id " +
                        "ORDER BY COUNT(a) DESC, MAX(a.submittedAt) DESC, a.quiz.id DESC",
                Long.class
        ).setMaxResults(limit).getResultList();
    }
}
