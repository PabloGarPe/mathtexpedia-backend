package mathtexpedia.es.api.persistence.pdfCommentLike;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class PdfCommentLikeDao extends GenericJPADao implements PdfCommentLikeDataService {

    @Override
    @Transactional
    public boolean insertIfAbsent(long commentId, long userId, Instant now) {
        logger.trace("Saving like of user {} on comment {}", userId, commentId);

        return em.createNativeQuery("""
                        INSERT INTO pdf_comment_like (comment_id, user_id, created_at)
                        VALUES (:commentId, :userId, :now)
                        ON CONFLICT (comment_id, user_id) DO NOTHING
                        """)
                .setParameter("commentId", commentId)
                .setParameter("userId", userId)
                .setParameter("now", now)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional
    public boolean delete(long commentId, long userId) {
        logger.trace("Deleting like of user {} on comment {}", userId, commentId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaDelete<PdfCommentLike> delete = cb.createCriteriaDelete(PdfCommentLike.class);
        Root<PdfCommentLike> root = delete.from(PdfCommentLike.class);
        delete.where(cb.equal(root.get("comment").get("id"), commentId), cb.equal(root.get("userAccount").get("id"), userId));

        return session.createMutationQuery(delete).executeUpdate() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countByComment(Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty())
            return Map.of();

        logger.trace("Counting likes of {} comments", commentIds.size());

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PdfCommentLike> root = cq.from(PdfCommentLike.class);
        cq.multiselect(root.get("comment").get("id"), cb.count(root))
                .where(root.get("comment").get("id").in(commentIds))
                .groupBy(root.get("comment").get("id"));

        Map<Long, Long> counts = new HashMap<>();
        for (Tuple row : session.createQuery(cq).getResultList())
            counts.put(row.get(0, Long.class), row.get(1, Long.class));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findLikedIds(long userId, Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty())
            return Set.of();

        logger.trace("Getting likes of user {} on {} comments", userId, commentIds.size());

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PdfCommentLike> root = cq.from(PdfCommentLike.class);
        cq.select(root.get("comment").<Long>get("id"))
                .where(cb.equal(root.get("userAccount").get("id"), userId), root.get("comment").get("id").in(commentIds));

        List<Long> ids = session.createQuery(cq).getResultList();
        return new HashSet<>(ids);
    }
}
