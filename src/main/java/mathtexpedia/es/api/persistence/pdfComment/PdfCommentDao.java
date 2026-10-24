package mathtexpedia.es.api.persistence.pdfComment;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class PdfCommentDao extends GenericJPADao implements PdfCommentDataService {

    private static final String ID_PARAM = "id";

    @Override
    @Transactional
    public PdfComment create(PdfComment comment) {
        logger.trace("Creating comment of user {} on PDF {}", comment.getUserAccount().getId(), comment.getPdf().getId());

        em.persist(comment);
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PdfComment> findById(long id) {
        logger.trace("Getting comment {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root).where(cb.equal(root.get("id"), id));

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserAndStatus(long userId, CommentStatus status) {
        logger.trace("Counting {} comments of user {}", status, userId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        cq.select(cb.count(root)).where(cb.equal(root.get("userAccount").get("id"), userId), cb.equal(root.get("status"), status));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfComment> findVisibleRoots(long pdfId, int offset, int limit) {
        logger.trace("Getting visible root comments of PDF {} (offset {}, limit {})", pdfId, offset, limit);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root)
                .where(visibleRoot(cb, cq, root, pdfId))
                .orderBy(cb.desc(root.get("publishedAt")), cb.desc(root.get("id")));

        return session.createQuery(cq).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countVisibleRoots(long pdfId) {
        logger.trace("Counting visible root comments of PDF {}", pdfId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        cq.select(cb.count(root)).where(visibleRoot(cb, cq, root, pdfId));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfComment> findPublishedReplies(long rootId, int offset, int limit) {
        logger.trace("Getting published replies of comment {} (offset {}, limit {})", rootId, offset, limit);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root)
                .where(cb.equal(root.get("parent").get("id"), rootId), cb.equal(root.get("status"), CommentStatus.PUBLISHED))
                .orderBy(cb.asc(root.get("publishedAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countPublishedReplies(Collection<Long> rootIds) {
        if (rootIds == null || rootIds.isEmpty())
            return Map.of();

        logger.trace("Counting published replies of {} comments", rootIds.size());

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<PdfComment> root = cq.from(PdfComment.class);
        cq.multiselect(root.get("parent").get("id"), cb.count(root))
                .where(root.get("parent").get("id").in(rootIds), cb.equal(root.get("status"), CommentStatus.PUBLISHED))
                .groupBy(root.get("parent").get("id"));

        Map<Long, Long> counts = new HashMap<>();
        for (Tuple row : session.createQuery(cq).getResultList())
            counts.put(row.get(0, Long.class), row.get(1, Long.class));
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfComment> findByUserAndPdf(long userId, long pdfId) {
        logger.trace("Getting comments of user {} on PDF {}", userId, pdfId);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root)
                .where(cb.equal(root.get("userAccount").get("id"), userId),
                        cb.equal(root.get("pdf").get("id"), pdfId),
                        cb.notEqual(root.get("status"), CommentStatus.DELETED))
                .orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PdfComment> findOldestPending() {
        logger.trace("Getting the oldest pending comment");

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root)
                .where(cb.equal(root.get("status"), CommentStatus.PENDING))
                .orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdfComment> findByStatuses(Collection<CommentStatus> statuses, int offset, int limit) {
        if (statuses == null || statuses.isEmpty())
            return List.of();

        logger.trace("Getting comments in {} (offset {}, limit {})", statuses, offset, limit);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PdfComment> cq = cb.createQuery(PdfComment.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        fetchAssociations(root);
        cq.select(root)
                .where(root.get("status").in(statuses))
                .orderBy(cb.asc(root.get("createdAt")), cb.asc(root.get("id")));

        return session.createQuery(cq).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatuses(Collection<CommentStatus> statuses) {
        if (statuses == null || statuses.isEmpty())
            return 0;

        logger.trace("Counting comments in {}", statuses);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PdfComment> root = cq.from(PdfComment.class);
        cq.select(cb.count(root)).where(root.get("status").in(statuses));

        return session.createQuery(cq).getSingleResult();
    }

    @Override
    @Transactional
    public boolean publish(long id, Set<CommentStatus> from, Instant now) {
        logger.trace("Publishing comment {}", id);

        if (from == null || from.isEmpty())
            return false;

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfComment> update = cb.createCriteriaUpdate(PdfComment.class);
        Root<PdfComment> root = update.from(PdfComment.class);

        update.set(root.get("status"), CommentStatus.PUBLISHED);
        // Solo se fija la primera vez: restaurar un comentario ya publicado no lo cambia de posición
        update.set(root.<Instant>get("publishedAt"), cb.coalesce(root.<Instant>get("publishedAt"), now));
        update.set(root.get("moderatedAt"), now);
        update.set(root.<ModerationCategory>get("moderationCategory"), cb.nullLiteral(ModerationCategory.class));
        update.where(cb.equal(root.get("id"), id), root.get("status").in(from));

        return session.createMutationQuery(update).executeUpdate() > 0;
    }

    @Override
    @Transactional
    public boolean reject(long id, Set<CommentStatus> from, ModerationCategory category, Instant now) {
        logger.trace("Rejecting comment {} with category {}", id, category);

        return moderate(id, from, CommentStatus.REJECTED, category, now);
    }

    @Override
    @Transactional
    public boolean sendToReview(long id, Set<CommentStatus> from, ModerationCategory hint, Instant now) {
        logger.trace("Sending comment {} to review with hint {}", id, hint);

        return moderate(id, from, CommentStatus.REVIEW, hint, now);
    }

    @Override
    @Transactional
    public boolean hide(long id) {
        logger.trace("Hiding comment {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfComment> update = cb.createCriteriaUpdate(PdfComment.class);
        Root<PdfComment> root = update.from(PdfComment.class);

        update.set(root.get("status"), CommentStatus.HIDDEN);
        update.where(cb.equal(root.get("id"), id), cb.equal(root.get("status"), CommentStatus.PUBLISHED));

        return session.createMutationQuery(update).executeUpdate() > 0;
    }

    @Override
    @Transactional
    public void markDeleted(long id, Instant now) {
        logger.trace("Deleting comment {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfComment> update = cb.createCriteriaUpdate(PdfComment.class);
        Root<PdfComment> root = update.from(PdfComment.class);

        update.set(root.get("status"), CommentStatus.DELETED);
        update.set(root.get("deletedAt"), now);
        update.set(root.get("body"), "");
        update.where(cb.equal(root.get("id"), id), cb.notEqual(root.get("status"), CommentStatus.DELETED));

        session.createMutationQuery(update).executeUpdate();
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public int incrementModerationAttempts(long id) {
        logger.trace("Incrementing moderation attempts of comment {}", id);

        // SQL nativo: ni JPQL ni Criteria soportan UPDATE ... RETURNING, que da el valor nuevo de forma atómica
        List<Number> result = em.createNativeQuery("""
                        UPDATE pdf_comment
                        SET moderation_attempts = moderation_attempts + 1
                        WHERE id = :id
                        RETURNING moderation_attempts
                        """)
                .setParameter(ID_PARAM, id)
                .getResultList();

        return result.isEmpty() ? 0 : result.get(0).intValue();
    }

    @Override
    @Transactional
    public int deleteStale(Instant rejectedBefore) {
        logger.trace("Deleting deleted comments and rejected ones moderated before {}", rejectedBefore);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaDelete<PdfComment> delete = cb.createCriteriaDelete(PdfComment.class);
        Root<PdfComment> root = delete.from(PdfComment.class);

        Subquery<Long> replies = delete.subquery(Long.class);
        Root<PdfComment> reply = replies.from(PdfComment.class);
        replies.select(reply.get("id")).where(cb.equal(reply.get("parent").get("id"), root.get("id")));

        Predicate staleRejected = cb.and(cb.equal(root.get("status"), CommentStatus.REJECTED),
                cb.lessThan(root.<Instant>get("moderatedAt"), rejectedBefore));
        delete.where(cb.or(staleRejected, cb.equal(root.get("status"), CommentStatus.DELETED)), cb.not(cb.exists(replies)));

        return session.createMutationQuery(delete).executeUpdate();
    }

    private boolean moderate(long id, Set<CommentStatus> from, CommentStatus to, ModerationCategory category, Instant now) {
        if (from == null || from.isEmpty())
            return false;

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfComment> update = cb.createCriteriaUpdate(PdfComment.class);
        Root<PdfComment> root = update.from(PdfComment.class);

        update.set(root.get("status"), to);
        update.set(root.get("moderationCategory"), category);
        update.set(root.get("moderatedAt"), now);
        update.where(cb.equal(root.get("id"), id), root.get("status").in(from));

        return session.createMutationQuery(update).executeUpdate() > 0;
    }

    /** Carga con el comentario todo lo que hace falta para mapearlo a DTO fuera de la transacción (solo relaciones a uno, así que se puede paginar). */
    private void fetchAssociations(Root<PdfComment> root) {
        root.fetch("userAccount");
        root.fetch("pdf");
        root.fetch("parent", JoinType.LEFT);
        root.fetch("replyTo", JoinType.LEFT).fetch("userAccount", JoinType.LEFT);
    }

    /** Raíces de un PDF que se listan en público: publicadas, o con alguna respuesta publicada (se pintan como no disponibles). */
    private Predicate visibleRoot(CriteriaBuilder cb, CommonAbstractCriteria query, Root<PdfComment> root, long pdfId) {
        Subquery<Long> replies = query.subquery(Long.class);
        Root<PdfComment> reply = replies.from(PdfComment.class);
        replies.select(reply.get("id")).where(
                cb.equal(reply.get("parent").get("id"), root.get("id")),
                cb.equal(reply.get("status"), CommentStatus.PUBLISHED));

        return cb.and(
                cb.equal(root.get("pdf").get("id"), pdfId),
                cb.isNull(root.get("parent")),
                cb.or(cb.equal(root.get("status"), CommentStatus.PUBLISHED), cb.exists(replies)));
    }
}
