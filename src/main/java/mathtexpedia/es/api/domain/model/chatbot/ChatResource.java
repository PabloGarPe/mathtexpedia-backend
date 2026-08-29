package mathtexpedia.es.api.domain.model.chatbot;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResource {

    public enum ChatContentType { PDF, BLOG_POST, PAGE }

    @Schema(description = "Tipo de contenido al que apunta el recurso")
    private ChatContentType type;

    @Schema(description = "Título del recurso")
    private String title;

    @Schema(description = "URL del recurso en Mathtexpedia")
    private String url;
}
