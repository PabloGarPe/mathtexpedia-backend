package mathtexpedia.es.api.presentation.chatbot;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mathtexpedia.es.api.domain.model.chatbot.ChatRequest;
import mathtexpedia.es.api.domain.model.chatbot.ChatResponse;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.presentation.GenericController;
import mathtexpedia.es.api.service.chatbot.ChatbotService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController extends GenericController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal UserProfile user,
                             @RequestBody @Valid ChatRequest request,
                             HttpServletRequest httpRequest) {
        String userIdentifier = user != null ? user.getEmail() : getClientIp(httpRequest);
        return chatbotService.chat(request, user != null, userIdentifier);
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
    }
}
