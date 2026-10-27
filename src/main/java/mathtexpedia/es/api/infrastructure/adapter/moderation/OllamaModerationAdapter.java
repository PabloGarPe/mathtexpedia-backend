package mathtexpedia.es.api.infrastructure.adapter.moderation;

import jakarta.annotation.PostConstruct;
import mathtexpedia.es.api.domain.exception.ModerationInvalidOutputException;
import mathtexpedia.es.api.domain.exception.ModerationTimeoutException;
import mathtexpedia.es.api.domain.exception.ModerationUnavailableException;
import mathtexpedia.es.api.domain.model.pdfComment.ModerationVerdict;
import mathtexpedia.es.api.domain.port.moderation.CommentModerationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

@Component
public class OllamaModerationAdapter implements CommentModerationPort {

    private static final Logger logger = LoggerFactory.getLogger(OllamaModerationAdapter.class);

    private final ChatModel chatModel;
    private final OllamaApi ollamaApi;
    private final ModerationPromptBuilder promptBuilder;
    private final BeanOutputConverter<ModelVerdict> converter = new BeanOutputConverter<>(ModelVerdict.class);
    private final String model;

    public OllamaModerationAdapter(
            ChatModel chatModel,
            OllamaApi ollamaApi,
            ModerationPromptBuilder promptBuilder,
            @Value("${moderation.ollama.model}") String model
    ) {
        this.chatModel = chatModel;
        this.ollamaApi = ollamaApi;
        this.promptBuilder = promptBuilder;
        this.model = model.trim();
    }

    @PostConstruct
    void warnIfNotConfigured() {
        if (model.isEmpty())
            logger.warn("moderation.ollama.model is empty: AI moderation is disabled and comments will stay PENDING");
    }

    @Override
    public ModerationVerdict moderate(String text) throws ModerationUnavailableException, ModerationInvalidOutputException {
        if (model.isEmpty()) throw new ModerationUnavailableException("moderation.ollama.model name is not configured");

        Prompt prompt = promptBuilder.build(text, OllamaChatOptions.builder()
                .model(model)
                .temperature(0.0)
                .disableThinking()
                .outputSchema(converter.getJsonSchema())
                .build());

        ChatResponse response;
        try {
            response = chatModel.call(prompt);
        } catch (RuntimeException e) {
            throw translate(e);
        }

        return VerdictMapper.toDomain(parse(response));
    }

    @Override
    public boolean isAvailable() {
        if (model.isEmpty()) return false;

        try {
            return ollamaApi.listModels().models().stream()
                    .anyMatch(installed -> sameModel(model, installed.name()) || sameModel(model, installed.model()));
        } catch (RuntimeException e) {
            logger.warn("Failed to check Ollama models availability: {}", e.toString());
            return false;
        }
    }

    private ModelVerdict parse(ChatResponse response) throws ModerationInvalidOutputException {
        Generation generation = response == null ? null : response.getResult();
        String content = generation == null ? null : generation.getOutput().getText();
        if (content == null || content.isBlank())
            throw new ModerationInvalidOutputException("The model returned no content");

        try {
            return converter.convert(content);
        } catch (RuntimeException e) {
            throw new ModerationInvalidOutputException("The model returned an invalid response", e);
        }
    }

    private ModerationUnavailableException translate(RuntimeException e) {
        if (isTimeout(e))
            return new ModerationTimeoutException("Ollama timed out", e);
        return new ModerationUnavailableException("ollama call failed: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    }

    private static boolean isTimeout(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause())
            if (t instanceof SocketTimeoutException ||
                    t instanceof HttpTimeoutException ||
                    t instanceof TimeoutException)
                return true;
        return false;
    }

    private static boolean sameModel(String configured, String installed) {
        return installed != null && (installed.equals(configured) || installed.equals(configured +
                ":latest"));
    }
}
