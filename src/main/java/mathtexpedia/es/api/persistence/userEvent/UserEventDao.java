package mathtexpedia.es.api.persistence.userEvent;

import mathtexpedia.es.api.domain.model.userEvent.EventType;
import mathtexpedia.es.api.persistence.GenericJPADao;
import mathtexpedia.es.api.persistence.user.UserAccount;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserEventDao extends GenericJPADao implements UserEventDataService {

    @Override
    @Transactional
    public void record(UserAccount user, EventType type, String payload) {
        logger.trace("Recording user event for user {} with type {} and payload {}", user, type, payload);

        UserEvent userEvent = new UserEvent();
        userEvent.setUserAccount(user);
        userEvent.setEventType(type);
        userEvent.setPayload(payload);
        userEvent.setOccurredAt(java.time.Instant.now());

        em.persist(userEvent);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Long, Long> countPdfViews(Collection<Long> pdfIds) {
        if (pdfIds == null || pdfIds.isEmpty())
            return Map.of();

        logger.trace("Counting views for {} PDFs", pdfIds.size());

        List<Object[]> rows = em.createNativeQuery("""
                SELECT CAST(e.payload ->> 'pdfId' AS bigint) AS pdf_id, COUNT(*)
                FROM user_event e
                WHERE e.event_type = 'PDF_VIEWED'
                  AND CAST(e.payload ->> 'pdfId' AS bigint) IN (:pdfIds)
                GROUP BY 1
                """)
                .setParameter("pdfIds", pdfIds)
                .getResultList();

        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows)
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> getTopViewedPdfIds(int limit) {
        logger.trace("Getting top {} most viewed PDFs", limit);

        List<Number> ids = em.createNativeQuery("""
                SELECT p.id
                FROM user_event e
                JOIN pdf p ON p.id = CAST(e.payload ->> 'pdfId' AS bigint)
                WHERE e.event_type = 'PDF_VIEWED'
                GROUP BY p.id
                ORDER BY COUNT(*) DESC, MAX(e.occurred_at) DESC, p.id DESC
                LIMIT :limit
                """)
                .setParameter("limit", limit)
                .getResultList();

        return ids.stream().map(Number::longValue).toList();
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> getTopViewedPdfIdsByUser(long userAccountId, int limit) {
        logger.trace("Getting top {} most viewed PDFs for user {}", limit, userAccountId);

        List<Number> ids = em.createNativeQuery("""
                SELECT p.id
                FROM user_event e
                JOIN pdf p ON p.id = CAST(e.payload ->> 'pdfId' AS bigint)
                WHERE e.event_type = 'PDF_VIEWED'
                  AND e.user_id = :userId
                GROUP BY p.id
                ORDER BY COUNT(*) DESC, MAX(e.occurred_at) DESC, p.id DESC
                LIMIT :limit
                """)
                .setParameter("userId", userAccountId)
                .setParameter("limit", limit)
                .getResultList();

        return ids.stream().map(Number::longValue).toList();
    }
}
