package mathtexpedia.es.api.service.chatbot;

import mathtexpedia.es.api.domain.model.chatbot.ChatRequest;
import mathtexpedia.es.api.domain.model.chatbot.ChatResponse;
import mathtexpedia.es.api.domain.security.UserProfile;

public interface ChatbotService {
    ChatResponse chat(ChatRequest request, UserProfile user, String clientIp);
}
