package mathtexpedia.es.api.persistence.chatbot;

import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.LocalDate;
import java.util.Optional;

public interface ChatUsageDataService {
    Optional<ChatUsage> get(String userIdentifier, LocalDate date);
    Optional<ChatUsage> get(UserAccount user, LocalDate date);
    void incrementUsage(String userIdentifier, LocalDate date, int tokens);
    void incrementUsage(UserAccount user, LocalDate date, int tokens);
}
