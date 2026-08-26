package mathtexpedia.es.api.domain.model.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChatMessage {

    @NotBlank
    @Pattern(regexp = "user|assistant")
    private String role;

    @NotBlank
    private String content;
}
