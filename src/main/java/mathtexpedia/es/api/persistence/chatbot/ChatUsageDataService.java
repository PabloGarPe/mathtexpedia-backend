package mathtexpedia.es.api.persistence.chatbot;

import java.time.LocalDate;
import java.util.Optional;

public interface ChatUsageDataService {
    Optional<ChatUsage> get(String userIdentifier, LocalDate date);
    void incrementUsage(String userIdentifier, LocalDate date, int tokens);
}
