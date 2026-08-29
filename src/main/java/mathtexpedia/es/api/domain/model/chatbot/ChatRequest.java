package mathtexpedia.es.api.domain.model.chatbot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    @Schema(description = "Mensaje del usuario para el chatbot", example = "¿Qué es una matriz diagonalizable?")
    @NotBlank
    private String message;

    @Schema(description = "Historial de la conversación, en orden cronológico. Opcional")
    @Nullable
    @Valid
    private List<ChatMessage> conversationHistory;
}
