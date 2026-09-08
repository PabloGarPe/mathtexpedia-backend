package mathtexpedia.es.api.service.chatbot;

import mathtexpedia.es.api.domain.model.chatbot.ChatRequest;
import mathtexpedia.es.api.domain.model.chatbot.ChatResponse;

public interface ChatbotService {
    ChatResponse chat(ChatRequest request, boolean isAuthenticated, String userIdentifier);
}
