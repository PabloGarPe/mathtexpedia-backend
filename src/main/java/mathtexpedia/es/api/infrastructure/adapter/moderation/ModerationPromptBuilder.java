package mathtexpedia.es.api.infrastructure.adapter.moderation;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ModerationPromptBuilder {

    static final String OPEN = "<<<COMENTARIO";
    static final String CLOSE = "COMENTARIO>>>";

    private static final Pattern DELIMITERS = Pattern.compile(Pattern.quote(OPEN) + "|" +
            Pattern.quote(CLOSE), Pattern.CASE_INSENSITIVE);

    private final String systemPrompt;

    ModerationPromptBuilder(
            @Value("classpath:moderation/system-prompt.txt") Resource systemPromptResource
    ) throws IOException {
        this.systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    Prompt build(String commentText, ChatOptions options) {
        return new Prompt(
                List.of(new SystemMessage(systemPrompt),
                        new UserMessage(wrap(commentText))),
                options
        );
    }

    static String wrap(String text) {
        return OPEN + "\n" + sanitize(text) + "\n" + CLOSE;
    }

    static String sanitize(String text) {
        String current = text == null ? "" : text;
        String previous;
        do {
            previous = current;
            current = DELIMITERS.matcher(previous).replaceAll("");
        } while (!current.equals(previous));
        return current;
    }
}
