package mathtexpedia.es.api.domain.model.chatbot;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    @NotBlank
    private String message;

    @Nullable
    @Valid
    private List<ChatMessage> conversationHistory;
}
