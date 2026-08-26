package mathtexpedia.es.api.domain.model.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SitemapEntry {

    private String title;
    private String loc;
    private String category;
}
