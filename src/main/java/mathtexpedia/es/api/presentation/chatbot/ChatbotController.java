package mathtexpedia.es.api.presentation.chatbot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Chatbot", description = "Asistente conversacional sobre el contenido de Mathtexpedia")
@RestController
@RequestMapping("/chatbot")
public class ChatbotController extends GenericController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @Operation(summary = "Envía un mensaje al chatbot",
            description = "Acepta tanto peticiones anónimas como autenticadas; si se envía un JWT válido, "
                    + "la respuesta puede tener en cuenta el contexto del usuario",
            security = { @SecurityRequirement })
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
