package mathtexpedia.es.api.domain.model.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResource {

    public enum ChatContentType { PDF, BLOG_POST, PAGE }

    private ChatContentType type;
    private String title;
    private String url;
}
