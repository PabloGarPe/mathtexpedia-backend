package mathtexpedia.es.api.persistence.pdfComment;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.domain.model.pdfComment.CommentStatus;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationCategory;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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

        return Optional.ofNullable(em.find(PdfComment.class, id));
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
    public boolean markDeleted(long id, Instant now) {
        logger.trace("Deleting comment {}", id);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaUpdate<PdfComment> update = cb.createCriteriaUpdate(PdfComment.class);
        Root<PdfComment> root = update.from(PdfComment.class);

        update.set(root.get("status"), CommentStatus.DELETED);
        update.set(root.get("deletedAt"), now);
        update.set(root.get("body"), "");
        update.where(cb.equal(root.get("id"), id), cb.notEqual(root.get("status"), CommentStatus.DELETED));

        return session.createMutationQuery(update).executeUpdate() > 0;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public int incrementModerationAttempts(long id) {
        logger.trace("Incrementing moderation attempts of comment {}", id);

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
        Predicate inFrom = root.get("status").in(from);
        update.where(cb.equal(root.get("id"), id), inFrom);

        return session.createMutationQuery(update).executeUpdate() > 0;
    }
}
