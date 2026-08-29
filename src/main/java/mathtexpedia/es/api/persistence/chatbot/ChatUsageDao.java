package mathtexpedia.es.api.persistence.chatbot;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import mathtexpedia.es.api.persistence.GenericJPADao;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ChatUsageDao extends GenericJPADao implements ChatUsageDataService {
    @Override
    @Transactional(readOnly = true)
    public Optional<ChatUsage> get(String userIdentifier, LocalDate date) {
        logger.trace("Getting usage for user: {} on date: {}", userIdentifier, date);

        Session session = em.unwrap(Session.class);
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<ChatUsage> cq = cb.createQuery(ChatUsage.class);
        Root<ChatUsage> root = cq.from(ChatUsage.class);
        cq.select(root).where(
                cb.equal(root.get("identifier"), userIdentifier),
                cb.equal(root.get("usageDate"), date)
        );

        return session.createQuery(cq).getResultList().stream().findFirst();
    }

    @Override
    @Transactional
    public void incrementUsage(String userIdentifier, LocalDate date, int tokens) {
        int updated = em.createQuery(
                "UPDATE ChatUsage c SET c.tokensUsed = c.tokensUsed + :tokens, " +
                        "c.requestCount = c.requestCount + 1 " +
                        "WHERE c.identifier = :userIdentifier AND c.usageDate = :date ")
                .setParameter("tokens", tokens)
                .setParameter("userIdentifier", userIdentifier)
                .setParameter("date", date)
                .executeUpdate();

        if (updated == 0) {
            ChatUsage usage = new ChatUsage();
            usage.setIdentifier(userIdentifier);
            usage.setTokensUsed(tokens);
            usage.setRequestCount(1);
            usage.setUsageDate(date);
            em.persist(usage);
        }
    }
}
