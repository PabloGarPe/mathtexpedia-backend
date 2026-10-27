package mathtexpedia.es.api.persistence.pdfCommentReport;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.domain.model.pdfComment.ReportReason;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PdfCommentReportDao extends GenericJPADao implements PdfCommentReportDataService {

    @Override
    @Transactional
    public boolean insertIfAbsent(long commentId, long reporterId, ReportReason reason, Instant now) {
        logger.trace("Saving report of user {} on comment {}", reporterId, commentId);

        return em.createNativeQuery("""
                        INSERT INTO pdf_comment_report (comment_id, reporter_id, reason, resolved, created_at)
                        VALUES (:commentId, :reporterId, :reason, false, :now)
                        ON CONFLICT (comment_id, reporter_id) DO NOTHING
                        """)
                .setParameter("commentId", commentId)
                .setParameter("reporterId", reporterId)
                .setParameter("reason", reason.name())
                .setParameter("now", now)
                .executeUpdate() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countOpenReporters(long commentId) {
        logger.trace("Counting open reports of comment {}", commentId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PdfCommentReport> root = cq.from(PdfCommentReport.class);
        cq.select(cb.countDistinct(root.get("reporter").get("id")))
                .where(cb.equal(root.get("comment").get("id"), commentId), cb.isFalse(root.get("resolved")));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional
    public int resolveAll(long commentId) {
        logger.trace("Resolving reports of comment {}", commentId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfCommentReport> update = cb.createCriteriaUpdate(PdfCommentReport.class);
        Root<PdfCommentReport> root = update.from(PdfCommentReport.class);
        update.set(root.get("resolved"), true);
        update.where(cb.equal(root.get("comment").get("id"), commentId), cb.isFalse(root.get("resolved")));

        return session.createMutationQuery(update).executeUpdate();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Map<ReportReason, Long>> countOpenByReason(Collection<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty())
            return Map.of();

        logger.trace("Counting open reports of {} comments", commentIds.size());

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PdfCommentReport> root = cq.from(PdfCommentReport.class);
        cq.multiselect(root.get("comment").get("id"), root.get("reason"), cb.count(root))
                .where(root.get("comment").get("id").in(commentIds), cb.isFalse(root.get("resolved")))
                .groupBy(root.get("comment").get("id"), root.get("reason"));

        List<Tuple> rows = session.createQuery(cq).getResultList();

        Map<Long, Map<ReportReason, Long>> result = new HashMap<>();
        for (Tuple row : rows)
            result.computeIfAbsent(row.get(0, Long.class), id -> new EnumMap<>(ReportReason.class))
                    .put(row.get(1, ReportReason.class), row.get(2, Long.class));
        return result;
    }
}
