package mathtexpedia.es.api.persistence.pdfRating;

import mathtexpedia.es.api.domain.model.pdfRating.PdfVote;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PdfRatingDao extends GenericJPADao implements PdfRatingDataService {

    private static final String PDF_PARAM = "pdfId";
    private static final String USER_PARAM = "userId";
    private static final String PDF_IDS_PARAM = "pdfIds";

    @Override
    @Transactional
    public void upsert(long pdfId, long userAccountId, PdfVote vote) {
        logger.trace("Saving vote {} of user {} on PDF {}", vote, userAccountId, pdfId);

        Instant now = Instant.now();
        em.createNativeQuery("""
                        INSERT INTO pdf_rating (pdf_id, user_id, vote, created_at, updated_at)
                        VALUES (:pdfId, :userId, :vote, :now, :now)
                        ON CONFLICT (pdf_id, user_id) DO UPDATE SET vote = EXCLUDED.vote, updated_at = EXCLUDED.updated_at
                        """)
                .setParameter(PDF_PARAM, pdfId)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter("vote", vote.name())
                .setParameter("now", now)
                .executeUpdate();
    }

    @Override
    @Transactional
    public boolean delete(long pdfId, long userAccountId) {
        logger.trace("Deleting vote of user {} on PDF {}", userAccountId, pdfId);

        return em.createQuery("DELETE FROM PdfRating r WHERE r.pdf.id = :pdfId AND r.userAccount.id = :userId")
                .setParameter(PDF_PARAM, pdfId)
                .setParameter(USER_PARAM, userAccountId)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public int deleteByUsers(long pdfId, Collection<Long> userAccountIds) {
        if (userAccountIds == null || userAccountIds.isEmpty())
            return 0;

        logger.trace("Deleting votes of {} users on PDF {}", userAccountIds.size(), pdfId);

        return em.createQuery("DELETE FROM PdfRating r WHERE r.pdf.id = :pdfId AND r.userAccount.id IN (:userIds)")
                .setParameter(PDF_PARAM, pdfId)
                .setParameter("userIds", userAccountIds)
                .executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PdfVote> getVote(long pdfId, long userAccountId) {
        logger.trace("Getting vote of user {} on PDF {}", userAccountId, pdfId);

        return em.createQuery("SELECT r.vote FROM PdfRating r WHERE r.pdf.id = :pdfId AND r.userAccount.id = :userId", PdfVote.class)
                .setParameter(PDF_PARAM, pdfId)
                .setParameter(USER_PARAM, userAccountId)
                .getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, PdfVote> getVotes(long userAccountId, Collection<Long> pdfIds) {
        if (pdfIds == null || pdfIds.isEmpty())
            return Map.of();

        logger.trace("Getting votes of user {} on {} PDFs", userAccountId, pdfIds.size());

        List<Object[]> rows = em.createQuery(
                        "SELECT r.pdf.id, r.vote FROM PdfRating r WHERE r.userAccount.id = :userId AND r.pdf.id IN (:pdfIds)",
                        Object[].class)
                .setParameter(USER_PARAM, userAccountId)
                .setParameter(PDF_IDS_PARAM, pdfIds)
                .getResultList();

        Map<Long, PdfVote> votes = new HashMap<>();
        for (Object[] row : rows)
            votes.put((Long) row[0], (PdfVote) row[1]);
        return votes;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Long, PdfVoteCounts> countVotes(Collection<Long> pdfIds) {
        if (pdfIds == null || pdfIds.isEmpty())
            return Map.of();

        logger.trace("Counting votes of {} PDFs", pdfIds.size());

        List<Object[]> rows = em.createNativeQuery("""
                SELECT r.pdf_id,
                       COUNT(*) FILTER (WHERE r.vote = 'LIKE'),
                       COUNT(*) FILTER (WHERE r.vote = 'DISLIKE')
                FROM pdf_rating r
                WHERE r.pdf_id IN (:pdfIds)
                GROUP BY r.pdf_id
                """)
                .setParameter(PDF_IDS_PARAM, pdfIds)
                .getResultList();

        Map<Long, PdfVoteCounts> counts = new HashMap<>();
        for (Object[] row : rows)
            counts.put(((Number) row[0]).longValue(),
                    new PdfVoteCounts(((Number) row[1]).longValue(), ((Number) row[2]).longValue()));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> getTopRatedPdfIds(int minVotes, int limit) {
        logger.trace("Getting top {} rated PDFs (min votes: {})", limit, minVotes);

        // Mismo cálculo que PdfRatingScore.wilson (z = 1.96, z² = 3.8416)
        List<Number> ids = em.createNativeQuery("""
                WITH c AS (
                    SELECT r.pdf_id,
                           COUNT(*) FILTER (WHERE r.vote = 'LIKE')::float8 AS l,
                           COUNT(*)::float8 AS n
                    FROM pdf_rating r
                    GROUP BY r.pdf_id
                    HAVING COUNT(*) >= :minVotes
                       AND COUNT(*) FILTER (WHERE r.vote = 'LIKE') > COUNT(*) FILTER (WHERE r.vote = 'DISLIKE')
                )
                SELECT p.id
                FROM c JOIN pdf p ON p.id = c.pdf_id
                ORDER BY ((c.l / c.n) + 3.8416 / (2 * c.n)
                          - 1.96 * sqrt(((c.l / c.n) * (1 - c.l / c.n) + 3.8416 / (4 * c.n)) / c.n))
                         / (1 + 3.8416 / c.n) DESC,
                         c.l DESC, p.id DESC
                LIMIT :limit
                """)
                .setParameter("minVotes", minVotes)
                .setParameter("limit", limit)
                .getResultList();

        return ids.stream().map(Number::longValue).toList();
    }
}
