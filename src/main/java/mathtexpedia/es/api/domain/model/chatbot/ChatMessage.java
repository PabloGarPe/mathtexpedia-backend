package mathtexpedia.es.api.domain.model.chatbot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChatMessage {

    @Schema(description = "Quién emitió el mensaje", allowableValues = {"user", "assistant"})
    @NotBlank
    @Pattern(regexp = "user|assistant")
    private String role;

    @Schema(description = "Contenido del mensaje")
    @NotBlank
    private String content;
}
